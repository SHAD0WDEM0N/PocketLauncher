package com.example.pocketlauncher.ui.settings

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.theme.PocketAccentPreset
import com.example.pocketlauncher.theme.PocketBackgroundStyle
import com.example.pocketlauncher.theme.PocketThemeMode
import com.example.pocketlauncher.ui.common.ConsoleMenuScreen

@Composable
fun ThemeSettingsScreen(
    mode: PocketThemeMode,
    accent: PocketAccentPreset,
    background: PocketBackgroundStyle,
    selectedIndex: Int,
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    ConsoleMenuScreen(
        title = "THEME",
        subtitle = "Changes apply immediately and are saved automatically.",
        items = listOf(
            "Mode     ·  ${mode.displayName}",
            "Accent   ·  ${accent.displayName}",
            "Backdrop ·  ${background.displayName}",
        ),
        selectedIndex = selectedIndex,
        footer = "LEFT / RIGHT OR A  CHANGE     B  BACK",
        touchMode = touchMode,
        primaryControllerLabel = "A  CHANGE",
        primaryTouchLabel = "CHANGE",
        onItemClick = onItemClick,
        onBack = onBack,
    )
}
