package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Barber
import com.example.ui.theme.BarberGold
import com.example.ui.theme.BarberGoldVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurfaceVariant

@Composable
fun HeroNextTurnCard(
    barber: Barber?,
    onNextTurn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isButtonFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF151E33),
                        Color(0xFF0F1728),
                        Color(0xFF1E2433)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = BarberGold.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        val isNarrow = maxWidth < 580.dp

        if (barber == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay barberos en lista de espera",
                    fontSize = 17.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (isNarrow) {
            // =========================================================
            // PORTRAIT (MÓVIL VERTICAL): Stacked layout
            // =========================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Image
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(BarberGold, Color(0xFFFDE68A), BarberGoldVariant, BarberGold)
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF18181B)),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUrl = barber.avatarUrl?.ifBlank { null } ?: barber.selfieUrl
                        if (!photoUrl.isNullOrBlank()) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .size(180, 180)
                                    .crossfade(150)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = barber.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = barber.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                            Text(
                                text = initial,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = BarberGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BarberGold)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "TURNO #1",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            BarberAvailabilityBadge(status = barber.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = barber.fullName,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            lineHeight = 22.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Entrada: ${barber.horaEntrada ?: "---"}",
                                fontSize = 12.sp,
                                color = EmeraldLive,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " • ${barber.completedCountToday} cortes",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Full-width Action Button for Portrait Mobile
                Box(
                    modifier = Modifier
                        .testTag("hero_next_turn_button")
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isButtonFocused) {
                                Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                            } else {
                                Brush.verticalGradient(listOf(BarberGold, BarberGoldVariant))
                            }
                        )
                        .border(
                            width = if (isButtonFocused) 3.dp else 1.dp,
                            color = if (isButtonFocused) ElectricCyan else BarberGoldVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .onFocusChanged { isButtonFocused = it.isFocused }
                        .focusable()
                        .clickable { onNextTurn() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PASAR TURNO AL SIGUIENTE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Pasar turno",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // =========================================================
            // LANDSCAPE (TV BOX / TABLET / CELULAR HORIZONTAL)
            // =========================================================
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Barber Photo & Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Image with Gold Glowing Ring
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(BarberGold, Color(0xFFFDE68A), BarberGoldVariant, BarberGold)
                                )
                            )
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF18181B)),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUrl = barber.avatarUrl?.ifBlank { null } ?: barber.selfieUrl
                        if (!photoUrl.isNullOrBlank()) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .size(180, 180)
                                    .crossfade(150)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = barber.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = barber.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                            Text(
                                text = initial,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = BarberGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Barber Details
                    Column {
                        // Badge "⚡ EN TURNO #1"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BarberGold)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TURNO #1 • LE TOCA ATENDER",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextDark,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))
                            BarberAvailabilityBadge(status = barber.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BarberAvailabilityDot(
                                status = barber.status,
                                size = 12.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Text(
                                text = barber.fullName,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                lineHeight = 30.sp
                            )
                        }

                        Text(
                            text = barber.role.ifEmpty { "Barbero Profesional" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BarberGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Arrival & Cut Stats Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvSurfaceVariant)
                                    .border(1.dp, TvBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = EmeraldLive,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Entrada: ${barber.horaEntrada ?: "---"}",
                                        fontSize = 12.sp,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvSurfaceVariant)
                                    .border(1.dp, TvBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Atendidos hoy: ${barber.completedCountToday}",
                                    fontSize = 12.sp,
                                    color = BarberGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Right Side: Action Button "PASAR TURNO" with TV Focus Highlight
                Box(
                    modifier = Modifier
                        .testTag("hero_next_turn_button")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isButtonFocused) {
                                Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                            } else {
                                Brush.verticalGradient(listOf(BarberGold, BarberGoldVariant))
                            }
                        )
                        .border(
                            width = if (isButtonFocused) 4.dp else 1.dp,
                            color = if (isButtonFocused) ElectricCyan else BarberGoldVariant,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .onFocusChanged { isButtonFocused = it.isFocused }
                        .focusable()
                        .clickable { onNextTurn() }
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LLEGÓ CLIENTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isButtonFocused) Color(0xFF1E293B) else Color.Black.copy(alpha = 0.75f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Pasar Turno",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Pasar turno",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
