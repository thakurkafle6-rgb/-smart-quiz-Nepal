package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.Black,
    primaryContainer = NepalBlue,
    onPrimaryContainer = Color.White,
    secondary = NeonCrimson,
    onSecondary = Color.White,
    tertiary = NeonGold,
    onTertiary = Color.Black,
    background = SlateBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = CardBackgroundDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NepalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = NepalBlue,
    secondary = NepalCrimson,
    onSecondary = Color.White,
    tertiary = NepalGold,
    onTertiary = Color.Black,
    background = WhiteBackground,
    onBackground = Color(0xFF0F172A),
    surface = CardBackgroundLight,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = NepalCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
