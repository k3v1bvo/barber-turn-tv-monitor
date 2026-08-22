package com.example.data.model

data class Barber(
    val id: String,
    val fullName: String,
    val avatarUrl: String?,
    val role: String = "barbero",
    val horaEntrada: String?, // e.g. "08:30:00"
    val completedCountToday: Int = 0,
    val lastCompletedAt: String? = null, // e.g. "2026-08-12T09:15:00Z"
    val status: BarberStatus = BarberStatus.DISPONIBLE,
    val activeClientName: String? = null,
    val currentService: String? = null
)

enum class BarberStatus {
    EN_TURNO,      // #1 in queue (Next to serve)
    DISPONIBLE,    // In queue waiting
    EN_CORTE,      // Currently cutting a client
    DESCANSO       // Not present / on break
}

enum class TvOverlayMode {
    FULLSCREEN_PANEL,        // Standard TV Dashboard
    OVERLAY_TRANSPARENT,     // Semi-transparent overlay over music video
    COMPACT_FLOATING_WIDGET, // Floating corner popup widget
    BOTTOM_BANNER_STRIP,     // Small horizontal banner strip at the bottom
    MINI_CORNER_PILL,        // Ultra-discrete mini pill in top/bottom corner
    HIDDEN                   // Turn overlay hidden (video/music 100% full screen)
}

enum class BackgroundMediaType {
    LOFI_BARBER_BEATS,   // Relaxing Barber Lofi Stream
    REGGAETON_HITS,      // Latin/Reggaeton Salon Hits
    POP_ENGLISH_RADIO,   // Top Hits Radio
    CUSTOM_YOUTUBE_URL   // Custom Video/Stream
}

data class TurnBoardState(
    val queuedBarbers: List<Barber> = emptyList(),
    val activeBarbers: List<Barber> = emptyList(),
    val rotationOffset: Int = 0,
    val shopName: String = "BarberSite - Control de Turnos",
    val isLiveSupabase: Boolean = false,
    val isDemoMode: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastRefreshTime: String = "",
    val totalCutsToday: Int = 0,
    val barbersPresentCount: Int = 0,
    val overlayMode: TvOverlayMode = TvOverlayMode.BOTTOM_BANNER_STRIP,
    val bgMediaType: BackgroundMediaType = BackgroundMediaType.LOFI_BARBER_BEATS,
    val customMediaUrl: String = "",
    val isVideoPlaying: Boolean = true,
    val lastRemoteSyncTime: String = ""
)

data class SupabaseSettings(
    val url: String = "https://xyzcompany.supabase.co",
    val apiKey: String = "",
    val shopName: String = "BarberSite - Control de Turnos",
    val isDemoMode: Boolean = true,
    val refreshIntervalSec: Int = 3
)
