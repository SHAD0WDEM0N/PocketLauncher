package com.example.pocketlauncher.ui.settings

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.ui.common.ConsoleMenuScreen

@Composable
fun SettingsScreen(
    selectedIndex: Int,
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    ConsoleMenuScreen(
        title = "SETTINGS",
        items = listOf(
            "Front End Settings",
            "Emulator Settings",
            "System Settings",
        ),
        selectedIndex = selectedIndex,
        touchMode = touchMode,
        onItemClick = onItemClick,
        onBack = onBack,
    )
}

@Composable
fun FrontEndSettingsScreen(
    selectedIndex: Int,
    themeSummary: String = "Default",
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    ConsoleMenuScreen(
        title = "FRONT END SETTINGS",
        subtitle = "PocketLauncher appearance, artwork and controller options.",
        items = listOf(
            "Theme  ·  $themeSummary",
            "Artwork & Scraping  ·  TheGamesDB",
            "Controller / Input Test",
        ),
        selectedIndex = selectedIndex,
        touchMode = touchMode,
        onItemClick = onItemClick,
        onBack = onBack,
    )
}

@Composable
fun EmulatorSettingsScreen(
    selectedIndex: Int,
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    enabledPlatforms: Set<Platform>,
) {
    val enabled = Platform.entries
        .filter { it in enabledPlatforms }
        .joinToString(", ") { it.displayName }
        .ifBlank { "None" }

    ConsoleMenuScreen(
        title = "EMULATOR SETTINGS",
        subtitle = "Visible systems: $enabled",
        items = listOf(
            "Manage Systems",
            "Core Downloads  ·  Next milestone",
            "External Emulators  ·  Next milestone",
        ),
        selectedIndex = selectedIndex,
        touchMode = touchMode,
        onItemClick = onItemClick,
        onBack = onBack,
    )
}

@Composable
fun SystemManagerScreen(
    selectedIndex: Int,
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    enabledPlatforms: Set<Platform>,
) {
    val items = Platform.entries.map { platform ->
        val state = if (platform in enabledPlatforms) "ON" else "OFF"
        "${platform.displayName}     $state"
    }

    ConsoleMenuScreen(
        title = "MANAGE SYSTEMS",
        subtitle = "Only enabled systems appear on the PocketLauncher home screen.",
        items = items,
        selectedIndex = selectedIndex,
        footer = "A  TOGGLE SYSTEM     B  BACK",
        touchMode = touchMode,
        primaryControllerLabel = "A  TOGGLE",
        primaryTouchLabel = "TOGGLE",
        onItemClick = onItemClick,
        onBack = onBack,
    )
}


@Composable
fun CoreDownloadsScreen(
    installed: Boolean,
    downloading: Boolean,
    status: String,
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onRemove: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val label = when {
        downloading -> "mGBA     DOWNLOADING..."
        installed -> "mGBA     INSTALLED"
        else -> "mGBA     NOT INSTALLED"
    }

    ConsoleMenuScreen(
        title = "CORE DOWNLOADS",
        subtitle = "$status  ·  GB / GBC / GBA",
        items = listOf(label),
        selectedIndex = 0,
        touchMode = touchMode,
        primaryControllerLabel = if (installed) "A  VERIFY / LOAD" else "A  DOWNLOAD",
        primaryTouchLabel = if (installed) "VERIFY / LOAD" else "DOWNLOAD",
        secondaryControllerLabel = if (installed) "X  REMOVE" else null,
        secondaryTouchLabel = if (installed) "REMOVE" else null,
        onSecondaryAction = if (installed) onRemove else null,
        footer = if (installed) {
            "A  VERIFY / LOAD     X  REMOVE     B  BACK"
        } else {
            "A  DOWNLOAD mGBA     B  BACK"
        },
        onItemClick = onItemClick,
        onBack = onBack,
    )
}
