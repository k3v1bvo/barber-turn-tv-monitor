package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AsistenciaDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "profile_id") val profileId: String? = null,
    @Json(name = "barbero_id") val barberoId: String? = null,
    @Json(name = "fecha") val fecha: String? = null,
    @Json(name = "hora_entrada") val horaEntrada: String? = null,
    @Json(name = "hora_salida") val horaSalida: String? = null,
    @Json(name = "selfie_url") val selfieUrl: String? = null,
    @Json(name = "selfie") val selfie: String? = null,
    @Json(name = "foto_url") val fotoUrl: String? = null,
    @Json(name = "foto") val foto: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "en_almuerzo") val enAlmuerzo: Boolean? = false,
    @Json(name = "estado") val estado: String? = null,
    @Json(name = "profiles") val profiles: ProfileDto? = null
) {
    fun getEffectiveSelfie(): String? = selfieUrl?.ifBlank { null }
        ?: selfie?.ifBlank { null }
        ?: fotoUrl?.ifBlank { null }
        ?: foto?.ifBlank { null }
        ?: avatarUrl?.ifBlank { null }
}

@JsonClass(generateAdapter = true)
data class CitaDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "barbero_id") val barberoId: String? = null,
    @Json(name = "profile_id") val profileId: String? = null,
    @Json(name = "estado") val estado: String? = null,
    @Json(name = "fecha") val fecha: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "cliente_nombre") val clienteNombre: String? = null,
    @Json(name = "servicio") val servicio: String? = null
)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "foto_url") val fotoUrl: String? = null,
    @Json(name = "foto") val foto: String? = null,
    @Json(name = "role") val role: String? = null
) {
    fun getEffectiveAvatar(): String? = avatarUrl?.ifBlank { null }
        ?: avatar?.ifBlank { null }
        ?: fotoUrl?.ifBlank { null }
        ?: foto?.ifBlank { null }

    fun getEffectiveName(): String? = fullName?.ifBlank { null }
        ?: nombre?.ifBlank { null }
}

@JsonClass(generateAdapter = true)
data class TurnoRotacionDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "fecha") val fecha: String? = null,
    @Json(name = "rotation_offset") val rotationOffset: Int? = 0,
    @Json(name = "current_barbero_id") val currentBarberoId: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ConfigTurnosDto(
    @Json(name = "id") val id: String? = "turno_offset",
    @Json(name = "fecha") val fecha: String? = null,
    @Json(name = "rotation_offset") val rotationOffset: Int? = 0,
    @Json(name = "updated_at") val updatedAt: String? = null
)
