package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Barber

/**
 * MiniBarberoOverlay: Small, semi-transparent chip toggle fixed to the top-right corner.
 * Features subtle entry/exit and state transition animations when Supabase updates the barber data.
 * Uses a low-alpha Surface so it is unobtrusive for TV background content (video, series, music).
 */
@Composable
fun MiniBarberoOverlay(
    currentBarber: Barber?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .testTag("mini_barbero_overlay")
            .clip(RoundedCornerShape(24.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
        color = if (isFocused) Color(0x80000000) else Color(0x40000000), // Low alpha semi-transparent chip
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) Color.White else Color(0x33FFFFFF)
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(animationSpec = spring(stiffness = 500f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Live pulsing indicator dot
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
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B).copy(alpha = pulseAlpha))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Animated barber identity & availability
            AnimatedContent(
                targetState = currentBarber,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(200))
                        )
                },
                label = "barber_chip_transition"
            ) { barber ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (barber != null) {
                        BarberAvatar(
                            fullName = barber.fullName,
                            photoUrl = barber.avatarUrl ?: barber.selfieUrl,
                            size = 18.dp,
                            borderWidth = 1.dp,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))

                        // Subtle availability dot next to barber name
                        BarberAvailabilityDot(
                            status = barber.status,
                            size = 6.dp,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        Text(
                            text = "Turno: ${barber.fullName}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Sin turnos",
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Expand arrow icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expandir Lista",
                tint = if (isFocused) Color.White else Color(0x99FFFFFF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
