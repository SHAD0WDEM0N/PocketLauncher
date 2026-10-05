package com.example.pocketlauncher

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.ui.home.HomeScreen
import com.example.pocketlauncher.ui.emulation.EmulationScreen
import com.example.pocketlauncher.ui.input.InputTestScreen
import com.example.pocketlauncher.ui.platform.PlatformScreen
import com.example.pocketlauncher.ui.settings.EmulatorSettingsScreen
import com.example.pocketlauncher.ui.settings.CoreDownloadsScreen
import com.example.pocketlauncher.ui.settings.FrontEndSettingsScreen
import com.example.pocketlauncher.ui.settings.SettingsScreen
import com.example.pocketlauncher.ui.settings.SystemManagerScreen

@Composable
fun MainNavigation(
    uiState: PocketUiState,
    viewModel: MainViewModel,
) {
    when (uiState.screen) {
        Screen.HOME -> {
            val menuItems = buildList {
                add("Recently Played")
                Platform.entries
                    .filter { it in uiState.enabledPlatforms }
                    .forEach { add(it.displayName) }
                add("Settings")
            }

            HomeScreen(
                engineReady = uiState.engineReady,
                selectedIndex = uiState.menuIndex,
                menuItems = menuItems,
            )
        }

        Screen.PLATFORM -> {
            val platform = uiState.selectedPlatform
            if (platform != null) {
                PlatformScreen(
                    platform = platform,
                    folderLabel = uiState.currentFolderLabel,
                    games = uiState.games,
                    selectedIndex = uiState.gameIndex,
                    isScanning = uiState.isScanning,
                )
            }
        }

        Screen.SETTINGS -> SettingsScreen(
            selectedIndex = uiState.menuIndex,
        )

        Screen.FRONT_END_SETTINGS -> FrontEndSettingsScreen(
            selectedIndex = uiState.menuIndex,
        )

        Screen.EMULATOR_SETTINGS -> EmulatorSettingsScreen(
            selectedIndex = uiState.menuIndex,
            enabledPlatforms = uiState.enabledPlatforms,
        )

        Screen.SYSTEM_MANAGER -> SystemManagerScreen(
            selectedIndex = uiState.menuIndex,
            enabledPlatforms = uiState.enabledPlatforms,
        )

        Screen.CORE_DOWNLOADS -> CoreDownloadsScreen(
            installed = uiState.coreInstalled,
            downloading = uiState.coreDownloading,
            status = uiState.coreStatus,
        )

        Screen.EMULATION -> EmulationScreen(
            title = uiState.emulationTitle,
            status = uiState.emulationStatus,
            pixels = uiState.emulationFrame,
            width = uiState.emulationWidth,
            height = uiState.emulationHeight,
        )

        Screen.INPUT_TEST -> InputTestScreen(
            events = uiState.inputEvents,
            currentlyHeld = uiState.currentlyHeld,
        )
    }
}
