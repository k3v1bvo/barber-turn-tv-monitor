package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarberStatus

/**
 * Subtle color-coded indicator dot or badge next to each barber's name
 * to quickly communicate shift availability:
 * - Green (0xFF10B981): Active / Available on shift
 * - Amber (0xFFF59E0B): Active & Next in turn
 * - Blue (0xFF3B82F6): In service / Cutting
 * - Grey (0xFF71717A): Offline / On break
 */
@Composable
fun BarberAvailabilityDot(
    status: BarberStatus,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    pulsing: Boolean = true
) {
    val targetColor = when (status) {
        BarberStatus.DISPONIBLE -> Color(0xFF10B981) // Green / Active
        BarberStatus.EN_TURNO -> Color(0xFF10B981)   // Green / Active & Next
        BarberStatus.EN_CORTE -> Color(0xFF3B82F6)   // Blue / Cutting
        BarberStatus.DESCANSO -> Color(0xFF71717A)   // Grey / Offline
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "dot_color_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "availability_pulse")
    val alpha by if (pulsing && status != BarberStatus.DESCANSO) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 1.0f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static_alpha"
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(animatedColor.copy(alpha = alpha))
            .border(
                width = 1.dp,
                color = if (status == BarberStatus.DESCANSO) Color(0x40FFFFFF) else animatedColor.copy(alpha = 0.8f),
                shape = CircleShape
            )
    )
}

/**
 * Subtle pill badge next to a barber's name showing status dot + text (e.g. "Activo", "En corte", "Offline")
 * Features smooth entry/exit animations on Supabase status transitions.
 */
@Composable
fun BarberAvailabilityBadge(
    status: BarberStatus,
    modifier: Modifier = Modifier
) {
    val (targetColor, text, targetTextColor) = when (status) {
        BarberStatus.DISPONIBLE -> Triple(Color(0xFF10B981), "Activo", Color(0xFF6EE7B7))
        BarberStatus.EN_TURNO -> Triple(Color(0xFFF59E0B), "En turno", Color(0xFFFCD34D))
        BarberStatus.EN_CORTE -> Triple(Color(0xFF3B82F6), "En corte", Color(0xFF93C5FD))
        BarberStatus.DESCANSO -> Triple(Color(0xFF71717A), "Offline", Color(0xFFA1A1AA))
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetColor.copy(alpha = 0.15f),
        animationSpec = tween(350),
        label = "badge_bg_anim"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = targetColor.copy(alpha = 0.35f),
        animationSpec = tween(350),
        label = "badge_border_anim"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(350),
        label = "badge_text_anim"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarberAvailabilityDot(status = status, size = 6.dp, pulsing = status != BarberStatus.DESCANSO)
        Spacer(modifier = Modifier.width(4.dp))
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                (fadeIn(tween(250)) togetherWith fadeOut(tween(180)))
            },
            label = "badge_text_crossfade"
        ) { targetText ->
            Text(
                text = targetText,
                color = animatedTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
