package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BackgroundMediaType
import com.example.data.model.TvOverlayMode
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvSurface
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TvHeaderBar(
    shopName: String,
    isLiveSupabase: Boolean,
    isDemoMode: Boolean,
    lastRefreshTime: String,
    overlayMode: TvOverlayMode,
    bgMediaType: BackgroundMediaType,
    isVideoPlaying: Boolean,
    onNextTurn: () -> Unit,
    onRefresh: () -> Unit,
    onToggleOverlayMode: () -> Unit,
    onCycleMusic: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        while (true) {
            val now = Date()
            currentTimeStr = timeFormat.format(now)
            currentDateStr = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (overlayMode == TvOverlayMode.FULLSCREEN_PANEL) TvSurface else TvSurface.copy(alpha = 0.85f)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title & Status Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Barber Pole Accent Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BarberGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💈",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = shopName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isLiveSupabase) EmeraldLive else BarberGold)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = if (isLiveSupabase) "EN VIVO • SYNC REALTIME WEB" else if (isDemoMode) "MODO DEMO • SIMULACIÓN" else "SIN CONEXIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLiveSupabase) EmeraldLive else BarberGold
                        )

                        if (lastRefreshTime.isNotEmpty()) {
                            Text(
                                text = " ($lastRefreshTime)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Right Actions & TV Clock
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TV Clock
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = currentTimeStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BarberGold
                    )
                    Text(
                        text = currentDateStr,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Button: Overlay Mode Toggle (Pantalla / Transparente / Flotante / Barra / Pill / Oculto)
                val modeLabel = when (overlayMode) {
                    TvOverlayMode.FULLSCREEN_PANEL -> "Modo Transparente"
                    TvOverlayMode.OVERLAY_TRANSPARENT -> "Widget Flotante"
                    TvOverlayMode.COMPACT_FLOATING_WIDGET -> "Barra Inferior"
                    TvOverlayMode.BOTTOM_BANNER_STRIP -> "Pill Esquina"
                    TvOverlayMode.MINI_CORNER_PILL -> "Ocultar Turnos"
                    TvOverlayMode.HIDDEN -> "Pantalla Completa"
                }

                TvActionButton(
                    text = modeLabel,
                    icon = Icons.Default.OpenInNew,
                    bgColor = ElectricCyan,
                    textColor = Color.White,
                    testTag = "btn_overlay_mode",
                    onClick = onToggleOverlayMode
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Button: Music / Video Toggle
                val musicLabel = when (bgMediaType) {
                    BackgroundMediaType.LOFI_BARBER_BEATS -> "🎵 Música Lofi"
                    BackgroundMediaType.REGGAETON_HITS -> "🎶 Reggaeton Hits"
                    BackgroundMediaType.POP_ENGLISH_RADIO -> "📻 Radio Hits"
                    BackgroundMediaType.CUSTOM_YOUTUBE_URL -> "📹 Video Custom"
                }

                TvActionButton(
                    text = if (isVideoPlaying) musicLabel else "🔇 Sin Música",
                    icon = Icons.Default.MusicNote,
                    bgColor = Color(0xFF8B5CF6),
                    textColor = Color.White,
                    testTag = "btn_music_toggle",
                    onClick = onCycleMusic
                )

                Spacer(modifier = Modifier.width(8.dp))

                // TV Remote Action Button: Pasar Turno
                TvActionButton(
                    text = "Pasar Turno",
                    icon = Icons.Default.ChevronRight,
                    bgColor = BarberGold,
                    textColor = Color.Black,
                    testTag = "btn_next_turn",
                    onClick = onNextTurn
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Refresh Button
                TvActionButton(
                    text = "Refrescar",
                    icon = Icons.Default.Refresh,
                    bgColor = Color(0xFF334155),
                    textColor = Color.White,
                    testTag = "btn_refresh",
                    onClick = onRefresh
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Settings Button
                TvActionButton(
                    text = "Ajustes",
                    icon = Icons.Default.Settings,
                    bgColor = Color(0xFF1E293B),
                    textColor = Color.White,
                    testTag = "btn_settings",
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White else bgColor)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = BarberGold,
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isFocused) Color.Black else textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) Color.Black else textColor
            )
        }
    }
}
