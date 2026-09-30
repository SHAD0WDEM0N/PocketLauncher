package com.example.pocketlauncher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// PocketLauncher always uses a bespoke dark scheme.
// We deliberately do NOT follow the device dynamic colour.
private val PocketColorScheme = darkColorScheme(
    primary          = PocketAmber,
    onPrimary        = PocketBlack,
    primaryContainer = PocketAmberDim,
    background       = PocketBlack,
    onBackground     = PocketWhite,
    surface          = PocketSurface,
    onSurface        = PocketWhite,
    surfaceVariant   = PocketSurfaceAlt,
    onSurfaceVariant = PocketWhiteDim,
    outline          = PocketBorder,
    secondary        = PocketWhiteDim,
    onSecondary      = PocketBlack,
    error            = PocketRed,
)

@Composable
fun PocketLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PocketColorScheme,
        typography  = Typography,
        content     = content,
    )
}
