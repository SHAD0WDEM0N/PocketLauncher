package com.example.pocketlauncher.ui.settings

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.ui.common.ConsoleMenuScreen

@Composable
fun SettingsScreen(
    selectedIndex: Int,
) {
    ConsoleMenuScreen(
        title = "SETTINGS",
        items = listOf(
            "Front End Settings",
            "Emulator Settings",
            "System Settings",
        ),
        selectedIndex = selectedIndex,
    )
}

@Composable
fun FrontEndSettingsScreen(
    selectedIndex: Int,
) {
    ConsoleMenuScreen(
        title = "FRONT END SETTINGS",
        subtitle = "PocketLauncher appearance, artwork and controller options.",
        items = listOf(
            "Theme  ·  Default",
            "Artwork & Scraping  ·  TheGamesDB",
            "Controller / Input Test",
        ),
        selectedIndex = selectedIndex,
    )
}

@Composable
fun EmulatorSettingsScreen(
    selectedIndex: Int,
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
    )
}

@Composable
fun SystemManagerScreen(
    selectedIndex: Int,
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
    )
}


@Composable
fun CoreDownloadsScreen(
    installed: Boolean,
    downloading: Boolean,
    status: String,
) {
    val label = when {
        downloading -> "mGBA     DOWNLOADING..."
        installed -> "mGBA     INSTALLED"
        else -> "mGBA     NOT INSTALLED"
    }

    ConsoleMenuScreen(
        title = "CORE DOWNLOADS",
        subtitle = status,
        items = listOf(label),
        selectedIndex = 0,
        footer = if (installed) {
            "A  VERIFY / LOAD     X  REMOVE     B  BACK"
        } else {
            "A  DOWNLOAD mGBA     B  BACK"
        },
    )
}
