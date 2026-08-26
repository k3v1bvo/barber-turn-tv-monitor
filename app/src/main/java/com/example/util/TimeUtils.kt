package com.example.util

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {

    // Official Bolivia Time Zone (America/La_Paz, UTC-4)
    val ZONA_BOLIVIA: ZoneId = ZoneId.of("America/La_Paz")

    // 12-hour Formatter with AM/PM (e.g. "08:30 AM")
    private val FORMATO_HORA_BOLIVIA: DateTimeFormatter = DateTimeFormatter
        .ofPattern("hh:mm a", Locale.forLanguageTag("es-BO"))
        .withZone(ZONA_BOLIVIA)

    /**
     * Converts any ISO timestamp from Supabase (e.g. "2026-08-26T12:30:00Z" or "2026-08-26T08:30:00-04:00")
     * into exact Bolivia local time (UTC-4): "08:30 AM"
     */
    fun formatearHoraBolivia(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "--:--"
        return try {
            if (isoTimestamp.contains("T")) {
                try {
                    val instant = Instant.parse(isoTimestamp)
                    FORMATO_HORA_BOLIVIA.format(instant).uppercase()
                } catch (e: Exception) {
                    val odt = OffsetDateTime.parse(isoTimestamp)
                    val boliviaDateTime = odt.atZoneSameInstant(ZONA_BOLIVIA)
                    boliviaDateTime.format(FORMATO_HORA_BOLIVIA).uppercase()
                }
            } else if (isoTimestamp.contains(":")) {
                // Raw time "08:30:00"
                val parts = isoTimestamp.split(":")
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
                isoTimestamp
            }
        } catch (e: Exception) {
            val timePart = isoTimestamp.substringAfter("T").take(5)
            if (timePart.isNotBlank()) timePart else "--:--"
        }
    }

    /**
     * Returns today's date in Bolivia timezone as "YYYY-MM-DD"
     */
    fun getFechaHoyBolivia(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        return LocalDate.now(ZONA_BOLIVIA).format(formatter)
    }
}
