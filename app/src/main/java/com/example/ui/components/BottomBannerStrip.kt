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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BackgroundMediaType
import com.example.data.model.TurnBoardState
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface

@Composable
fun BottomBannerStrip(
    turnState: TurnBoardState,
    onNextTurn: () -> Unit,
    onRestoreFullscreen: () -> Unit,
    onCycleMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextBarber = turnState.queuedBarbers.firstOrNull()
    val upcomingBarber2 = turnState.queuedBarbers.getOrNull(1)
    val upcomingBarber3 = turnState.queuedBarbers.getOrNull(2)

    var isNextFocused by remember { mutableStateOf(false) }
    var isFullFocused by remember { mutableStateOf(false) }
    var isMusicFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(TvSurface.copy(alpha = 0.92f))
            .border(
                width = 2.dp,
                color = BarberGold,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section 1: Active Barber #1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.3f)
            ) {
                // Barber Avatar
                BarberAvatar(
                    fullName = nextBarber?.fullName,
                    photoUrl = nextBarber?.avatarUrl ?: nextBarber?.selfieUrl,
                    size = 46.dp,
                    borderWidth = 2.dp,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BarberGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LE TOCARÍA (#1)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        if (turnState.isLiveSupabase) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLive)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (nextBarber != null) {
                            BarberAvailabilityDot(
                                status = nextBarber.status,
                                size = 8.dp,
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }
                        Text(
                            text = nextBarber?.fullName ?: "Sin barberos en espera",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }

            // Section 2: Queue Preview (#2 & #3)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1.2f)
                    .padding(horizontal = 8.dp)
            ) {
                if (upcomingBarber2 != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, TvBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "SIGUIENTE #2",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BarberAvailabilityDot(
                                    status = upcomingBarber2.status,
                                    size = 6.dp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = upcomingBarber2.fullName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }

                if (upcomingBarber3 != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, TvBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "#3 EN FILA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BarberAvailabilityDot(
                                    status = upcomingBarber3.status,
                                    size = 6.dp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = upcomingBarber3.fullName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Action Buttons for TV D-Pad Remote
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Button: Pasar Turno
                Box(
                    modifier = Modifier
                        .testTag("banner_btn_next")
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isNextFocused) Color.White else BarberGold)
                        .border(
                            width = if (isNextFocused) 3.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .onFocusChanged { isNextFocused = it.isFocused }
                        .focusable()
                        .clickable { onNextTurn() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Pasar Turno",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PASAR TURNO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Button: Music Toggle
                Box(
                    modifier = Modifier
                        .testTag("banner_btn_music")
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isMusicFocused) Color.White else Color(0xFF8B5CF6))
                        .border(
                            width = if (isMusicFocused) 3.dp else 0.dp,
                            color = BarberGold,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .onFocusChanged { isMusicFocused = it.isFocused }
                        .focusable()
                        .clickable { onCycleMusic() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Cambiar Música",
                        tint = if (isMusicFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Button: Maximizar / Pantalla Completa
                Box(
                    modifier = Modifier
                        .testTag("banner_btn_fullscreen")
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFullFocused) Color.White else Color(0xFF334155))
                        .border(
                            width = if (isFullFocused) 3.dp else 0.dp,
                            color = BarberGold,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .onFocusChanged { isFullFocused = it.isFocused }
                        .focusable()
                        .clickable { onRestoreFullscreen() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Pantalla Completa",
                            tint = if (isFullFocused) Color.Black else TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Maximizar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullFocused) Color.Black else TextWhite
                        )
                    }
                }
            }
        }
    }
}
