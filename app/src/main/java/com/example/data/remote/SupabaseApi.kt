package com.example.data.remote

import com.example.data.model.AsistenciaDto
import com.example.data.model.CitaDto
import com.example.data.model.ProfileDto
import com.example.data.model.TurnoRotacionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface SupabaseApi {

    @GET
    suspend fun getAsistencias(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("fecha") fechaFilter: String, // e.g. "eq.2026-08-12"
        @Query("select") select: String = "*,profiles(id,full_name,avatar_url,role)"
    ): Response<List<AsistenciaDto>>

    @GET
    suspend fun getCitas(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("fecha") fechaFilter: String, // e.g. "eq.2026-08-12"
        @Query("select") select: String = "*"
    ): Response<List<CitaDto>>

    @GET
    suspend fun getProfiles(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): Response<List<ProfileDto>>

    @GET
    suspend fun getTurnoRotacion(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("fecha") fechaFilter: String,
        @Query("order") order: String = "updated_at.desc",
        @Query("limit") limit: Int = 1
    ): Response<List<TurnoRotacionDto>>

    @GET
    suspend fun getConfigTurnos(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String = "eq.turno_offset"
    ): Response<List<com.example.data.model.ConfigTurnosDto>>

    @POST
    suspend fun updateConfigTurnos(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") preferHeader: String = "resolution=merge-duplicates",
        @Body payload: com.example.data.model.ConfigTurnosDto
    ): Response<Unit>

    @POST
    suspend fun updateTurnoRotacion(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") preferHeader: String = "resolution=merge-duplicates",
        @Body payload: TurnoRotacionDto
    ): Response<Unit>
}
