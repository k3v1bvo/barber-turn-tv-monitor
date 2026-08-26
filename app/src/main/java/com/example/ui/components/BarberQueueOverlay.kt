package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Barber
import com.example.data.model.BarberStatus
import com.example.data.model.TurnBoardState

/**
 * BarberQueueOverlay: Bottom semi-transparent overlay displaying
 * the ordered list of barbers in queue and the "PASAR TURNO AL SIGUIENTE" action button.
 * Includes an internal state (isQueueVisible) to expand/collapse the queue overlay,
 * designed for TV Box remote control use while watching video content.
 */
@Composable
fun BarberQueueOverlay(
    turnState: TurnBoardState,
    onNextTurn: () -> Unit,
    onRestoreFullscreen: () -> Unit,
    onCycleMusic: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State variable controlling visibility of the barber queue cards (expanded/collapsed)
    var isQueueVisible by remember { mutableStateOf(false) }

    var isToggleFocused by remember { mutableStateOf(false) }
    var isPassTurnFocused by remember { mutableStateOf(false) }
    var isSettingsFocused by remember { mutableStateOf(false) }
    var isFullFocused by remember { mutableStateOf(false) }
    var isMusicFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // COLLAPSED MODE: Small, semi-transparent chip fixed to the top-right corner for TV background content
        AnimatedVisibility(
            visible = !isQueueVisible,
            enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)),
            exit = fadeOut(tween(200, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp)
        ) {
            MiniBarberoOverlay(
                currentBarber = turnState.queuedBarbers.firstOrNull(),
                onClick = { isQueueVisible = true }
            )
        }

        // EXPANDED MODE: Bottom semi-transparent queue overlay sheet with slide-up / slide-down animation
        AnimatedVisibility(
            visible = isQueueVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                color = Color(0xF209090B), // #09090b Zinc-950 semi-transparent dark background
                border = BorderStroke(1.dp, Color(0x40F59E0B)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // Header Bar: Title, Live Status Indicator & Control Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop Name & Live Status Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing amber indicator
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = pulseAlpha))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = turnState.shopName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Live or Demo pill
                    Box(
                        modifier = Modifier
                            .background(
                                if (turnState.isLiveSupabase) Color(0x2010B981) else Color(0x33F59E0B),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (turnState.isLiveSupabase) Color(0x5010B981) else Color(0x80F59E0B),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (turnState.isLiveSupabase) "EN VIVO" else "MODO DEMO",
                            color = if (turnState.isLiveSupabase) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Header Control Buttons (Toggle Queue, Music, Settings, Maximizar)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small Floating Toggle Button to Expand/Collapse Queue
                    Box(
                        modifier = Modifier
                            .testTag("overlay_toggle_expand")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToggleFocused) Color.White else Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isToggleFocused) Color.White else Color(0xFFF59E0B),
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { isToggleFocused = it.isFocused }
                            .focusable()
                            .clickable { isQueueVisible = !isQueueVisible }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isQueueVisible) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Plegar / Expandir Cola",
                                tint = if (isToggleFocused) Color.Black else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isQueueVisible) "Minimizar Cola" else "Ver Cola (${turnState.queuedBarbers.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isToggleFocused) Color.Black else Color(0xFFF59E0B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Music Button
                    Box(
                        modifier = Modifier
                            .testTag("overlay_btn_music")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMusicFocused) Color.White else Color(0xFF27272A))
                            .onFocusChanged { isMusicFocused = it.isFocused }
                            .focusable()
                            .clickable { onCycleMusic() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Música",
                                tint = if (isMusicFocused) Color.Black else Color(0xFFA1A1AA),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .testTag("overlay_btn_settings")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSettingsFocused) Color.White else Color(0xFF27272A))
                            .onFocusChanged { isSettingsFocused = it.isFocused }
                            .focusable()
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración API / Supabase",
                                tint = if (isSettingsFocused) Color.Black else Color(0xFFA1A1AA),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "API",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSettingsFocused) Color.Black else Color(0xFFA1A1AA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Maximizar Fullscreen Panel Button
                    Box(
                        modifier = Modifier
                            .testTag("overlay_btn_fullscreen")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFullFocused) Color.White else Color(0xFF27272A))
                            .onFocusChanged { isFullFocused = it.isFocused }
                            .focusable()
                            .clickable { onRestoreFullscreen() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Maximizar",
                                tint = if (isFullFocused) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Panel TV",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFullFocused) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // EXPANDED STATE: Full Barber Queue Cards List
            AnimatedVisibility(
                visible = isQueueVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    if (turnState.queuedBarbers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay barberos en lista de espera en este momento",
                                color = Color(0xFFA1A1AA),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(turnState.queuedBarbers) { index, barber ->
                                val pos = index + 1
                                BarberoTurnoCard(
                                    barber = barber,
                                    position = pos,
                                    esProximo = (pos == 1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button: "PASAR TURNO AL SIGUIENTE"
                    Surface(
                        modifier = Modifier
                            .testTag("btn_pasar_turno_siguiente")
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .onFocusChanged { isPassTurnFocused = it.isFocused }
                            .focusable()
                            .clickable { onNextTurn() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            width = if (isPassTurnFocused) 3.dp else 0.dp,
                            color = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )

                                Text(
                                    text = "PASAR TURNO AL SIGUIENTE",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

/**
 * BarberoTurnoCard: Individual barber item card adhering to specified design specs:
 * - Next up (#1): Golden gradient background, amber border, "🔥 TOCA ATENDER" solid badge
 * - Waiting (#2, #3...): Dark background #18181b, border #27272a, "EN ESPERA" badge
 */
@Composable
fun BarberoTurnoCard(
    barber: Barber,
    position: Int,
    esProximo: Boolean
) {
    val backgroundColor = if (esProximo) {
        Brush.horizontalGradient(listOf(Color(0x33F59E0B), Color(0x0DF59E0B)))
    } else {
        SolidColor(Color(0xFF18181B))
    }

    val borderColor = if (esProximo) Color(0x80F59E0B) else Color(0x1FA1A1AA)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Section: Avatar + Name + Entry Time + Completed Count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    // Circular Avatar (40dp)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF27272A))
                            .border(2.dp, Color(0x1FFFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!barber.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = barber.avatarUrl,
                                contentDescription = barber.fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show initial letter if no image
                            val initial = barber.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Position Badge (#1, #2...)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (esProximo) Color(0xFFFBBF24) else Color(0xFF27272A),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (esProximo) Color.Black else Color(0xFF3F3F46),
                                CircleShape
                            )
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$position",
                            color = if (esProximo) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Barber Details
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BarberAvailabilityDot(
                            status = barber.status,
                            size = 8.dp,
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        Text(
                            text = barber.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                        BarberAvailabilityBadge(status = barber.status)

                        if (barber.completedCountToday > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✓ ${barber.completedCountToday}",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0x1F10B981), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Entrada: ${formatHora(barber.horaEntrada)}",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                }
            }

            // Right Section: Status Badge
            if (esProximo) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🔥 TOCA ATENDER",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PRÓXIMO CLIENTE",
                        color = Color(0xFFF59E0B),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "EN ESPERA",
                    color = Color(0xFF71717A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Helper to cleanly format timestamps into HH:mm
 */
private fun formatHora(rawHora: String?): String {
    if (rawHora.isNullOrBlank()) return "--:--"
    return try {
        if (rawHora.contains("T")) {
            val timePart = rawHora.substringAfter("T").substringBefore(".")
            val parts = timePart.split(":")
            if (parts.size >= 2) "${parts[0]}:${parts[1]}" else rawHora
        } else if (rawHora.contains(":")) {
            val parts = rawHora.split(":")
            if (parts.size >= 2) "${parts[0]}:${parts[1]}" else rawHora
        } else {
            rawHora
        }
    } catch (e: Exception) {
        rawHora
    }
}
