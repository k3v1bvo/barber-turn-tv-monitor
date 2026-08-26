package com.example.data.repository

import com.example.data.model.Barber
import com.example.data.model.BarberStatus
import com.example.data.model.SupabaseSettings
import com.example.data.model.TurnBoardState
import com.example.data.model.TurnoRotacionDto
import com.example.data.remote.SupabaseApi
import com.example.data.remote.SupabaseClientProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TurnRepository {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private fun buildApi(baseUrl: String): SupabaseApi {
        val sanitizedUrl = baseUrl.trim().removeSuffix("/") + "/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }

    suspend fun fetchTurnBoardState(
        settings: SupabaseSettings,
        currentLocalOffset: Int
    ): TurnBoardState {
        val boliviaDate = try {
            java.time.LocalDate.now(java.time.ZoneOffset.ofHours(-4)).toString()
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        val todayStr = boliviaDate
        val lastRefreshStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        if (settings.url.isBlank() || settings.apiKey.isBlank()) {
            return TurnBoardState(
                shopName = settings.shopName,
                isLiveSupabase = false,
                isDemoMode = false,
                errorMessage = "Configure la URL y API Key de Supabase",
                lastRefreshTime = lastRefreshStr
            )
        }

        return try {
            val api = buildApi(settings.url)
            val authHeader = "Bearer ${settings.apiKey}"
            val endpointAsistencias = "${settings.url.trim().removeSuffix("/")}/rest/v1/asistencias"
            val endpointCitas = "${settings.url.trim().removeSuffix("/")}/rest/v1/citas"
            val endpointConfigTurnos = "${settings.url.trim().removeSuffix("/")}/rest/v1/config_turnos"
            val endpointRotacion = "${settings.url.trim().removeSuffix("/")}/rest/v1/turnos_rotacion"

            val asistenciasResp = api.getAsistencias(
                url = endpointAsistencias,
                apiKey = settings.apiKey,
                authHeader = authHeader,
                fechaFilter = "eq.$todayStr"
            )

            val citasResp = api.getCitas(
                url = endpointCitas,
                apiKey = settings.apiKey,
                authHeader = authHeader,
                fechaFilter = "eq.$todayStr"
            )

            // 1. Try fetching config_turnos (id = 'turno_offset')
            var effectiveOffset = currentLocalOffset
            var remoteSyncTimeStr = lastRefreshStr
            var syncSuccess = false

            try {
                val configResp = api.getConfigTurnos(
                    url = endpointConfigTurnos,
                    apiKey = settings.apiKey,
                    authHeader = authHeader,
                    idFilter = "eq.turno_offset"
                )
                if (configResp.isSuccessful && !configResp.body().isNullOrEmpty()) {
                    val cfg = configResp.body()!!.first()
                    if (cfg.fecha == todayStr) {
                        effectiveOffset = cfg.rotationOffset ?: 0
                    } else {
                        effectiveOffset = 0
                    }
                    if (!cfg.updatedAt.isNull_or_blank()) {
                        remoteSyncTimeStr = cfg.updatedAt ?: lastRefreshStr
                    }
                    syncSuccess = true
                }
            } catch (e: Exception) {
                // Fallback to turnos_rotacion below
            }

            if (!syncSuccess) {
                try {
                    val rotacionResp = api.getTurnoRotacion(
                        url = endpointRotacion,
                        apiKey = settings.apiKey,
                        authHeader = authHeader,
                        fechaFilter = "eq.$todayStr"
                    )
                    if (rotacionResp.isSuccessful && !rotacionResp.body().isNullOrEmpty()) {
                        val firstRot = rotacionResp.body()!!.first()
                        effectiveOffset = firstRot.rotationOffset ?: currentLocalOffset
                        if (!firstRot.updatedAt.isNull_or_blank()) {
                            remoteSyncTimeStr = firstRot.updatedAt ?: lastRefreshStr
                        }
                    }
                } catch (e: Exception) {
                    // Ignore if rotation table missing
                }
            }

            if (!asistenciasResp.isSuccessful) {
                return TurnBoardState(
                    shopName = settings.shopName,
                    isLiveSupabase = false,
                    isDemoMode = false,
                    errorMessage = "Error Supabase: HTTP ${asistenciasResp.code()} - ${asistenciasResp.message()}",
                    lastRefreshTime = lastRefreshStr
                )
            }

            val asistencias = asistenciasResp.body() ?: emptyList()
            val citas = citasResp.body() ?: emptyList()

            // 1. Filter & deduplicate present barbers today
            val presentAsistencias = asistencias
                .filter { !it.horaEntrada.isNull_or_blank() }
                .distinctBy { it.profileId ?: it.barberoId ?: it.profiles?.id ?: "unknown" }

            // Map completed cuts & active cuts per barber
            val completedCountMap = mutableMapOf<String, Int>()
            val lastCompletedAtMap = mutableMapOf<String, String>()
            val activeCitasMap = mutableMapOf<String, Pair<String?, String?>>()

            var totalCompletedToday = 0

            citas.forEach { cita ->
                val bId = cita.barberoId ?: cita.profileId ?: return@forEach
                val statusLower = cita.estado?.lowercase() ?: ""

                if (statusLower == "completado" || statusLower == "finalizado") {
                    completedCountMap[bId] = (completedCountMap[bId] ?: 0) + 1
                    totalCompletedToday++

                    val currentLast = lastCompletedAtMap[bId]
                    if (currentLast == null || (cita.updatedAt ?: "") > currentLast) {
                        lastCompletedAtMap[bId] = cita.updatedAt ?: ""
                    }
                } else if (statusLower == "en_proceso" || statusLower == "atendiendo" || statusLower == "en_corte") {
                    activeCitasMap[bId] = Pair(
                        cita.clienteNombre ?: "Cliente en sillón",
                        cita.servicio ?: "Corte / Barba"
                    )
                }
            }

            // Build Barber list
            val allPresentBarbers: List<Barber> = if (presentAsistencias.isNotEmpty()) {
                presentAsistencias.map { asistencia ->
                    val bId = asistencia.profileId ?: asistencia.barberoId ?: asistencia.profiles?.id ?: "unknown"
                    val name = asistencia.profiles?.fullName
                        ?.ifBlank { null }
                        ?: "Barbero #${bId.takeLast(4)}"
                    val avatar = asistencia.profiles?.avatarUrl
                    val arrival = asistencia.horaEntrada ?: "08:00:00"
                    val completed = completedCountMap[bId] ?: 0
                    val lastComp = lastCompletedAtMap[bId]
                    val activeInfo = activeCitasMap[bId]

                    Barber(
                        id = bId,
                        fullName = name,
                        avatarUrl = avatar,
                        role = asistencia.profiles?.role ?: "barbero",
                        horaEntrada = arrival,
                        completedCountToday = completed,
                        lastCompletedAt = lastComp,
                        status = if (activeInfo != null) BarberStatus.EN_CORTE else BarberStatus.DISPONIBLE,
                        activeClientName = activeInfo?.first,
                        currentService = activeInfo?.second
                    )
                }
            } else {
                // Fetch registered barbers from profiles table
                try {
                    val endpointProfiles = "${settings.url.trim().removeSuffix("/")}/rest/v1/profiles"
                    val profResp = api.getProfiles(
                        url = endpointProfiles,
                        apiKey = settings.apiKey,
                        authHeader = authHeader
                    )
                    val profList = profResp.body() ?: emptyList()
                    val filteredProf = profList.filter {
                        (it.role?.lowercase() ?: "barbero") == "barbero" || it.role.isNullOrBlank()
                    }
                    filteredProf.map { prof ->
                        val bId = prof.id ?: "unknown"
                        val name = prof.fullName?.ifBlank { null } ?: "Barbero #${bId.takeLast(4)}"
                        val completed = completedCountMap[bId] ?: 0
                        val lastComp = lastCompletedAtMap[bId]
                        val activeInfo = activeCitasMap[bId]

                        Barber(
                            id = bId,
                            fullName = name,
                            avatarUrl = prof.avatarUrl,
                            role = prof.role ?: "barbero",
                            horaEntrada = "Pendiente",
                            completedCountToday = completed,
                            lastCompletedAt = lastComp,
                            status = if (activeInfo != null) BarberStatus.EN_CORTE else BarberStatus.DISPONIBLE,
                            activeClientName = activeInfo?.first,
                            currentService = activeInfo?.second
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Separate active barbers from queue barbers
            val activeBarbers = allPresentBarbers.filter { it.status == BarberStatus.EN_CORTE }
            val queueEligible = allPresentBarbers.filter { it.status != BarberStatus.EN_CORTE }

            // Priority 1: 0 cuts today, sorted by arrival time
            val priority1 = queueEligible
                .filter { it.completedCountToday == 0 }
                .sortedBy { it.horaEntrada ?: "99:99" }

            // Priority 2: Has cuts today, sorted by last completed time
            val priority2 = queueEligible
                .filter { it.completedCountToday > 0 }
                .sortedBy { it.lastCompletedAt ?: "" }

            val rawQueue = priority1 + priority2

            // Apply rotation offset
            val shiftedQueue = if (rawQueue.isNotEmpty()) {
                val safeOffset = (effectiveOffset % rawQueue.size + rawQueue.size) % rawQueue.size
                rawQueue.drop(safeOffset) + rawQueue.take(safeOffset)
            } else {
                emptyList()
            }

            // Mark position #1 as EN_TURNO
            val finalQueue = shiftedQueue.mapIndexed { index, barber ->
                if (index == 0) {
                    barber.copy(status = BarberStatus.EN_TURNO)
                } else {
                    barber.copy(status = BarberStatus.DISPONIBLE)
                }
            }

            TurnBoardState(
                queuedBarbers = finalQueue,
                activeBarbers = activeBarbers,
                rotationOffset = effectiveOffset,
                shopName = settings.shopName,
                isLiveSupabase = true,
                isDemoMode = false,
                isLoading = false,
                errorMessage = if (allPresentBarbers.isEmpty()) "Sin barberos registrados en Supabase" else null,
                lastRefreshTime = lastRefreshStr,
                totalCutsToday = totalCompletedToday,
                barbersPresentCount = allPresentBarbers.size,
                lastRemoteSyncTime = remoteSyncTimeStr
            )
        } catch (e: Exception) {
            TurnBoardState(
                shopName = settings.shopName,
                isLiveSupabase = false,
                isDemoMode = false,
                errorMessage = "Error de red Supabase: ${e.localizedMessage ?: e.message}",
                lastRefreshTime = lastRefreshStr
            )
        }
    }

    suspend fun pushRemoteTurnNext(
        settings: SupabaseSettings,
        newOffset: Int,
        currentBarberId: String?
    ) {
        if (settings.isDemoMode || settings.url.isBlank() || settings.apiKey.isBlank()) return
        try {
            val api = buildApi(settings.url)
            val authHeader = "Bearer ${settings.apiKey}"
            val endpointConfigTurnos = "${settings.url.trim().removeSuffix("/")}/rest/v1/config_turnos"
            val endpointRotacion = "${settings.url.trim().removeSuffix("/")}/rest/v1/turnos_rotacion"

            val boliviaDate = try {
                java.time.LocalDate.now(java.time.ZoneOffset.ofHours(-4)).toString()
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }
            val todayStr = boliviaDate
            val isoNow = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

            try {
                api.updateConfigTurnos(
                    url = endpointConfigTurnos,
                    apiKey = settings.apiKey,
                    authHeader = authHeader,
                    payload = com.example.data.model.ConfigTurnosDto(
                        id = "turno_offset",
                        fecha = todayStr,
                        rotationOffset = newOffset,
                        updatedAt = isoNow
                    )
                )
            } catch (e: Exception) {
                // Ignore if config_turnos write fails or RLS restricts
            }

            try {
                api.updateTurnoRotacion(
                    url = endpointRotacion,
                    apiKey = settings.apiKey,
                    authHeader = authHeader,
                    payload = TurnoRotacionDto(
                        fecha = todayStr,
                        rotationOffset = newOffset,
                        currentBarberoId = currentBarberId,
                        updatedAt = isoNow
                    )
                )
            } catch (e: Exception) {
                // Ignore fallback
            }
        } catch (e: Exception) {
            // Ignore overall errors
        }
    }

    /**
     * Subscribes in real-time to Supabase changes on public schema tables:
     * - 'asistencias' (barber attendance / arrival)
     * - 'citas' (haircuts, services, queue transitions)
     * - 'turnos_rotacion' & 'config_turnos' (turn rotation offsets)
     *
     * Emits whenever a table change event occurs, enabling instant zero-lag UI updates.
     */
    fun subscribeToRealtimeChanges(settings: SupabaseSettings): Flow<Unit> = callbackFlow {
        if (settings.isDemoMode || settings.url.isBlank() || settings.apiKey.isBlank()) {
            return@callbackFlow
        }

        var channel: RealtimeChannel? = null
        var listenerJob: Job? = null

        try {
            val client = SupabaseClientProvider.getInstance(settings.url, settings.apiKey)
            client.realtime.connect()

            val channelName = "barber_turnos_realtime_${System.currentTimeMillis()}"
            val activeChannel = client.channel(channelName)
            channel = activeChannel

            val changesFlow = activeChannel.postgresChangeFlow<PostgresAction>(schema = "public")

            listenerJob = launch {
                changesFlow.collect {
                    trySend(Unit)
                }
            }

            activeChannel.subscribe()
        } catch (e: Exception) {
            // Log or fallback safely without crashing
        }

        awaitClose {
            listenerJob?.cancel()
            launch {
                try {
                    channel?.unsubscribe()
                } catch (e: Exception) {
                    // Ignore channel cleanup exception
                }
            }
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.isBlank()
    }
}
