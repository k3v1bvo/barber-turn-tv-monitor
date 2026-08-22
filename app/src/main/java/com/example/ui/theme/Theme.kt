package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BarberTvColorScheme = darkColorScheme(
    primary = BarberGold,
    onPrimary = TextDark,
    primaryContainer = BarberGoldVariant,
    onPrimaryContainer = TextWhite,
    secondary = ElectricCyan,
    onSecondary = TextDark,
    background = TvBackground,
    onBackground = TextWhite,
    surface = TvSurface,
    onSurface = TextWhite,
    surfaceVariant = TvSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = TvBorder,
    error = CrimsonAlert
)

@Composable
fun BarberTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BarberTvColorScheme,
        typography = Typography,
        content = content
    )
}
