package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.data.model.Barber
import com.example.data.model.TurnBoardState
import com.example.ui.theme.BarberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface

@Composable
fun CompactFloatingWidget(
    turnState: TurnBoardState,
    onNextTurn: () -> Unit,
    onRestoreFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextBarber = turnState.queuedBarbers.firstOrNull()
    val upcomingBarbers = turnState.queuedBarbers.drop(1).take(2)
    var isNextFocused by remember { mutableStateOf(false) }
    var isFullFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(TvSurface.copy(alpha = 0.92f))
            .border(2.dp, BarberGold, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // Widget Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldLive)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TURNO ACTUAL BARBERÍA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BarberGold
                    )
                }

                // Restore Fullscreen Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFullFocused) Color.White else Color(0xFF334155))
                        .onFocusChanged { isFullFocused = it.isFocused }
                        .focusable()
                        .clickable { onRestoreFullscreen() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Pantalla Completa",
                            tint = if (isFullFocused) Color.Black else TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Maximizar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullFocused) Color.Black else TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Active Barber (#1)
            if (nextBarber != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BarberGold.copy(alpha = 0.15f))
                        .border(1.dp, BarberGold, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BarberAvatar(
                                fullName = nextBarber.fullName,
                                photoUrl = nextBarber.avatarUrl ?: nextBarber.selfieUrl,
                                size = 52.dp,
                                borderWidth = 2.dp,
                                fontSize = 22.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BarberGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ LE TOCARÍA ATENDER (#1)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BarberAvailabilityDot(
                                        status = nextBarber.status,
                                        size = 8.dp,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = nextBarber.fullName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay barberos en espera",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }

            // Next 2 Barbers
            if (upcomingBarbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Siguientes en fila:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))

                upcomingBarbers.forEachIndexed { index, b ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BarberAvailabilityDot(
                                status = b.status,
                                size = 6.dp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "#${index + 2} ${b.fullName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                        Text(
                            text = b.horaEntrada ?: "En espera",
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Pasar Turno
            Box(
                modifier = Modifier
                    .testTag("widget_btn_next")
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isNextFocused) Color.White else BarberGold)
                    .border(
                        width = if (isNextFocused) 3.dp else 0.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onFocusChanged { isNextFocused = it.isFocused }
                    .focusable()
                    .clickable { onNextTurn() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Pasar Turno",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PASAR SIGUIENTE TURNO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
