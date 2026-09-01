package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.BarberGold

/**
 * BarberAvatar: Centralized, bulletproof Avatar component for TV & Mobile.
 * - Guarantees NO black boxes ever: Uses a warm gold/dark slate foundation.
 * - Displays barber's initial badge immediately while downloading or on error.
 * - Loads Coil ImageRequest with crossfade, memory cache, and hardware layer safety.
 */
@Composable
fun BarberAvatar(
    fullName: String?,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.5.dp,
    borderColor: Color = BarberGold,
    badgeColor: Color = BarberGold,
    initialColor: Color = Color.Black,
    fontSize: TextUnit = (size.value * 0.42f).sp
) {
    val cleanUrl = photoUrl?.trim()?.ifBlank { null }
    val initial = fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "💈"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        // Foundation: Initial letter in gold badge
        Text(
            text = initial,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = initialColor
        )

        // Overlay photo if available
        if (!cleanUrl.isNullOrBlank()) {
            val context = LocalContext.current
            val pixelSize = (size.value * 3f).toInt().coerceIn(96, 320)

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cleanUrl)
                    .size(pixelSize, pixelSize)
                    .crossfade(200)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .allowHardware(false)
                    .build(),
                contentDescription = fullName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    // While downloading: show golden foundation with subtle spinner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.45f),
                            color = initialColor,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    // On network/404 error: Fall back cleanly to the gold initial badge (NEVER BLACK!)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Black,
                            color = initialColor
                        )
                    }
                }
            )
        }
    }
}
