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
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

    /**
     * Formats an ISO-8601 timestamp (e.g. "2026-08-26T08:45:12-04:00") into
     * an attractive 12-hour format: "08:45 AM"
     */
    fun formatHoraLlegada(horaIso: String?): String {
        if (horaIso.isNullOrBlank()) return "Pendiente"
        return try {
            if (horaIso.contains("T")) {
                val odt = OffsetDateTime.parse(horaIso)
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-BO"))
                odt.format(formatter).uppercase()
            } else if (horaIso.contains(":")) {
                val parts = horaIso.split(":")
                val hour = parts[0].toIntOrNull() ?: 0
                val min = parts.getOrNull(1) ?: "00"
                val ampm = if (hour >= 12) "PM" else "AM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format(Locale.US, "%02d:%s %s", displayHour, min, ampm)
            } else {
                horaIso
            }
        } catch (e: Exception) {
            try {
                val instant = Instant.parse(horaIso)
                val zdt = instant.atZone(ZoneOffset.ofHours(-4))
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-BO"))
                zdt.format(formatter).uppercase()
            } catch (_: Exception) {
                val timePart = horaIso.substringAfter("T").take(5)
                if (timePart.isNotBlank()) timePart else horaIso
            }
        }
    }

    suspend fun fetchTurnBoardState(
        settings: SupabaseSettings,
        currentLocalOffset: Int
    ): TurnBoardState {
        val todayStr = com.example.util.TimeUtils.getFechaHoyBolivia()
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

            // 1. Fetch rotation offset from config_turnos (id = 'turno_offset')
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
                    if (!cfg.updatedAt.isNullOrBlank()) {
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
                        if (!firstRot.updatedAt.isNullOrBlank()) {
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
                .filter { !it.horaEntrada.isNullOrBlank() }
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
                    val name = asistencia.profiles?.fullName?.ifBlank { null } ?: "Barbero #${bId.takeLast(4)}"
                    val avatar = asistencia.profiles?.avatarUrl?.ifBlank { null } ?: asistencia.selfieUrl
                    val rawArrival = asistencia.horaEntrada ?: ""
                    val formattedArrival = com.example.util.TimeUtils.formatearHoraBolivia(rawArrival)
                    val completed = completedCountMap[bId] ?: 0
                    val lastComp = lastCompletedAtMap[bId]
                    val activeInfo = activeCitasMap[bId]
                    val isAlmuerzo = asistencia.enAlmuerzo ?: false

                    val initialStatus = when {
                        isAlmuerzo -> BarberStatus.EN_ALMUERZO
                        activeInfo != null -> BarberStatus.EN_CORTE
                        else -> BarberStatus.DISPONIBLE
                    }

                    Barber(
                        id = bId,
                        fullName = name,
                        avatarUrl = avatar,
                        selfieUrl = asistencia.selfieUrl,
                        role = asistencia.profiles?.role ?: "barbero",
                        horaEntrada = formattedArrival,
                        rawHoraEntrada = rawArrival,
                        enAlmuerzo = isAlmuerzo,
                        completedCountToday = completed,
                        lastCompletedAt = lastComp,
                        status = initialStatus,
                        activeClientName = activeInfo?.first,
                        currentService = activeInfo?.second
                    )
                }
            } else {
                // Fetch registered barbers from profiles table if no attendance recorded yet
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
                            selfieUrl = null,
                            role = prof.role ?: "barbero",
                            horaEntrada = "Pendiente",
                            rawHoraEntrada = null,
                            enAlmuerzo = false,
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

            // Separate active / lunch barbers from queue barbers
            val activeBarbers = allPresentBarbers.filter {
                it.status == BarberStatus.EN_CORTE || it.status == BarberStatus.EN_ALMUERZO
            }
            val queueEligible = allPresentBarbers.filter {
                it.status != BarberStatus.EN_CORTE && it.status != BarberStatus.EN_ALMUERZO
            }

            // Priority 1: 0 cuts today, sorted chronologically by raw arrival time
            val priority1 = queueEligible
                .filter { it.completedCountToday == 0 }
                .sortedBy { it.rawHoraEntrada ?: "9999" }

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

            val todayStr = com.example.util.TimeUtils.getFechaHoyBolivia()
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
                // Ignore if turnos_rotacion write fails
            }
        } catch (e: Exception) {
            // Ignore error
        }
    }

    fun subscribeToRealtimeChanges(settings: SupabaseSettings): Flow<Unit> = callbackFlow {
        if (settings.isDemoMode || settings.url.isBlank() || settings.apiKey.isBlank()) {
            awaitClose {}
            return@callbackFlow
        }

        var activeChannel: RealtimeChannel? = null
        var job: Job? = null

        try {
            val supabaseClient = SupabaseClientProvider.getInstance(settings.url, settings.apiKey)

            job = launch {
                activeChannel = supabaseClient.channel("barber-turns-tv-live")
                val channel = activeChannel ?: return@launch

                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public")

                channel.subscribe(blockUntilSubscribed = false)

                changeFlow.collect {
                    trySend(Unit)
                }
            }
        } catch (e: Exception) {
            // Log or ignore
        }

        awaitClose {
            job?.cancel()
            launch {
                try {
                    activeChannel?.unsubscribe()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
