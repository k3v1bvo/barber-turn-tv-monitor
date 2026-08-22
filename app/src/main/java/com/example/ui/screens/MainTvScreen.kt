package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BackgroundMediaType
import com.example.data.model.TvOverlayMode
import com.example.ui.components.ActiveCutsSection
import com.example.ui.components.BarberQueueOverlay
import com.example.ui.components.BottomBannerStrip
import com.example.ui.components.BottomTicker
import com.example.ui.components.CompactFloatingWidget
import com.example.ui.components.HeroNextTurnCard
import com.example.ui.components.HiddenModeOverlay
import com.example.ui.components.MiniCornerPill
import com.example.ui.components.QueueListSection
import com.example.ui.components.SupabaseSettingsDialog
import com.example.ui.components.TvBackgroundMediaPlayer
import com.example.ui.components.TvHeaderBar
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBackground
import com.example.data.viewmodel.TurnViewModel

@Composable
fun MainTvScreen(
    viewModel: TurnViewModel
) {
    val turnState by viewModel.turnState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()

    val nextBarberInTurn = turnState.queuedBarbers.firstOrNull()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TvBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Layer 1: Background Music / Video Stream Player
            if (turnState.isVideoPlaying) {
                TvBackgroundMediaPlayer(
                    mediaType = turnState.bgMediaType,
                    customUrl = turnState.customMediaUrl,
                    isPlaying = turnState.isVideoPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Layer 2: Main Content Layout depending on Overlay Mode
            when (turnState.overlayMode) {
                TvOverlayMode.HIDDEN -> {
                    // Overlay completely hidden (video/music 100% full screen)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        HiddenModeOverlay(
                            onRestoreOverlay = {
                                viewModel.setOverlayMode(TvOverlayMode.FULLSCREEN_PANEL)
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                TvOverlayMode.MINI_CORNER_PILL -> {
                    // Ultra-discrete corner pill in the corner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        MiniCornerPill(
                            turnState = turnState,
                            onCycleOverlayMode = {
                                viewModel.setOverlayMode(TvOverlayMode.HIDDEN)
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                TvOverlayMode.BOTTOM_BANNER_STRIP -> {
                    // Barber Queue Overlay at the Bottom of the screen / Top-Right collapsed chip
                    BarberQueueOverlay(
                        turnState = turnState,
                        onNextTurn = { viewModel.nextTurn() },
                        onRestoreFullscreen = {
                            viewModel.setOverlayMode(TvOverlayMode.FULLSCREEN_PANEL)
                        },
                        onCycleMusic = { cycleMusicGenre(turnState.bgMediaType, viewModel) },
                        onOpenSettings = { viewModel.openSettingsDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                TvOverlayMode.COMPACT_FLOATING_WIDGET -> {
                    // Floating Corner Widget Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Top Header Bar
                        TvHeaderBar(
                            shopName = turnState.shopName,
                            isLiveSupabase = turnState.isLiveSupabase,
                            isDemoMode = turnState.isDemoMode,
                            lastRefreshTime = turnState.lastRefreshTime,
                            overlayMode = turnState.overlayMode,
                            bgMediaType = turnState.bgMediaType,
                            isVideoPlaying = turnState.isVideoPlaying,
                            onNextTurn = { viewModel.nextTurn() },
                            onRefresh = { viewModel.fetchState() },
                            onToggleOverlayMode = {
                                viewModel.setOverlayMode(TvOverlayMode.BOTTOM_BANNER_STRIP)
                            },
                            onCycleMusic = { cycleMusicGenre(turnState.bgMediaType, viewModel) },
                            onOpenSettings = { viewModel.openSettingsDialog() }
                        )

                        // Floating Corner Widget
                        CompactFloatingWidget(
                            turnState = turnState,
                            onNextTurn = { viewModel.nextTurn() },
                            onRestoreFullscreen = {
                                viewModel.setOverlayMode(TvOverlayMode.FULLSCREEN_PANEL)
                            },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }

                TvOverlayMode.FULLSCREEN_PANEL, TvOverlayMode.OVERLAY_TRANSPARENT -> {
                    // Full Screen / Transparent Panel Layout
                    val bgOverlayColor = if (turnState.overlayMode == TvOverlayMode.OVERLAY_TRANSPARENT) {
                        Color.Black.copy(alpha = 0.65f)
                    } else if (turnState.isVideoPlaying) {
                        Color.Black.copy(alpha = 0.85f)
                    } else {
                        TvBackground
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgOverlayColor)
                    ) {
                        // TV Header Bar
                        TvHeaderBar(
                            shopName = turnState.shopName,
                            isLiveSupabase = turnState.isLiveSupabase,
                            isDemoMode = turnState.isDemoMode,
                            lastRefreshTime = turnState.lastRefreshTime,
                            overlayMode = turnState.overlayMode,
                            bgMediaType = turnState.bgMediaType,
                            isVideoPlaying = turnState.isVideoPlaying,
                            onNextTurn = { viewModel.nextTurn() },
                            onRefresh = { viewModel.fetchState() },
                            onToggleOverlayMode = {
                                val nextMode = when (turnState.overlayMode) {
                                    TvOverlayMode.FULLSCREEN_PANEL -> TvOverlayMode.OVERLAY_TRANSPARENT
                                    TvOverlayMode.OVERLAY_TRANSPARENT -> TvOverlayMode.COMPACT_FLOATING_WIDGET
                                    TvOverlayMode.COMPACT_FLOATING_WIDGET -> TvOverlayMode.BOTTOM_BANNER_STRIP
                                    TvOverlayMode.BOTTOM_BANNER_STRIP -> TvOverlayMode.MINI_CORNER_PILL
                                    TvOverlayMode.MINI_CORNER_PILL -> TvOverlayMode.HIDDEN
                                    TvOverlayMode.HIDDEN -> TvOverlayMode.FULLSCREEN_PANEL
                                }
                                viewModel.setOverlayMode(nextMode)
                            },
                            onCycleMusic = { cycleMusicGenre(turnState.bgMediaType, viewModel) },
                            onOpenSettings = { viewModel.openSettingsDialog() }
                        )

                        // Error Notice Banner if Supabase connection issue
                        if (turnState.errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CrimsonAlert)
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = TextWhite
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = turnState.errorMessage ?: "",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.openSettingsDialog() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Black,
                                            contentColor = TextWhite
                                        )
                                    ) {
                                        Text("Configurar Supabase", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Main TV Body Grid
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            // Top Hero: Barbero en Turno #1
                            HeroNextTurnCard(
                                barber = nextBarberInTurn,
                                onNextTurn = { viewModel.nextTurn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(175.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom Columns: Queue List & Active Cuts
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left: Remaining Queue (#2, #3, #4...)
                                QueueListSection(
                                    queuedBarbers = turnState.queuedBarbers,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .fillMaxSize()
                                )

                                // Right: Active Barber Cuts ("EN SILLÓN")
                                ActiveCutsSection(
                                    activeBarbers = turnState.activeBarbers,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                )
                            }
                        }

                        // Bottom Ticker
                        BottomTicker(
                            totalCutsToday = turnState.totalCutsToday,
                            barbersPresentCount = turnState.barbersPresentCount,
                            isLiveSupabase = turnState.isLiveSupabase
                        )
                    }
                }
            }

            // Settings Modal Dialog
            if (showSettingsDialog) {
                SupabaseSettingsDialog(
                    currentSettings = settings,
                    onSave = { url, key, shopName, isDemo ->
                        viewModel.saveSettings(url, key, shopName, isDemo)
                    },
                    onDismiss = { viewModel.closeSettingsDialog() }
                )
            }
        }
    }
}

private fun cycleMusicGenre(current: BackgroundMediaType, viewModel: TurnViewModel) {
    val next = when (current) {
        BackgroundMediaType.LOFI_BARBER_BEATS -> BackgroundMediaType.REGGAETON_HITS
        BackgroundMediaType.REGGAETON_HITS -> BackgroundMediaType.POP_ENGLISH_RADIO
        BackgroundMediaType.POP_ENGLISH_RADIO -> BackgroundMediaType.CUSTOM_YOUTUBE_URL
        BackgroundMediaType.CUSTOM_YOUTUBE_URL -> BackgroundMediaType.LOFI_BARBER_BEATS
    }
    viewModel.setBgMediaType(next)
}
