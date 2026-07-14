package com.pocketlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PocketRed,
    secondary = FocusBlue, // Electric blue for focus/secondary
    tertiary = PocketBlue,
    background = BackgroundBlack,
    surface = CardCharcoal,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = PocketRed.copy(alpha = 0.2f),
    secondaryContainer = FocusBlue.copy(alpha = 0.2f),
    surfaceVariant = CardCharcoal
)

@Composable
fun PocketLauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
