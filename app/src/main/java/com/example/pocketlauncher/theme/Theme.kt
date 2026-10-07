package com.example.pocketlauncher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun darkPocketColorScheme(accent: PocketAccentPreset) = darkColorScheme(
    primary = accent.color,
    onPrimary = PocketBlack,
    primaryContainer = accent.color.copy(alpha = 0.72f),
    background = PocketBlack,
    onBackground = PocketWhite,
    surface = PocketSurface,
    onSurface = PocketWhite,
    surfaceVariant = PocketSurfaceAlt,
    onSurfaceVariant = PocketWhiteDim,
    outline = PocketBorder,
    secondary = PocketWhiteDim,
    onSecondary = PocketBlack,
    error = PocketRed,
)

private fun lightPocketColorScheme(accent: PocketAccentPreset) = lightColorScheme(
    primary = accent.color,
    onPrimary = Color(0xFF101010),
    primaryContainer = accent.color.copy(alpha = 0.22f),
    background = Color(0xFFF1F1EF),
    onBackground = Color(0xFF171717),
    surface = Color(0xFFF8F8F6),
    onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFE6E6E2),
    onSurfaceVariant = Color(0xFF4F4F4B),
    outline = Color(0xFFB9B9B2),
    secondary = Color(0xFF555550),
    onSecondary = Color(0xFFF8F8F6),
    error = PocketRed,
)

@Composable
fun PocketLauncherTheme(
    mode: PocketThemeMode = PocketThemeMode.DARK,
    accent: PocketAccentPreset = PocketAccentPreset.AMBER,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = when (mode) {
            PocketThemeMode.DARK -> darkPocketColorScheme(accent)
            PocketThemeMode.LIGHT -> lightPocketColorScheme(accent)
        },
        typography = Typography,
        content = content,
    )
}
