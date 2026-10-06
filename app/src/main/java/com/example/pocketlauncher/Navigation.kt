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
import com.example.pocketlauncher.ui.settings.ScraperSettingsScreen
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
                manufacturers = Platform.entries.associate { it.displayName to it.manufacturer },
                releaseYears = Platform.entries.associate { it.displayName to it.releaseYear },
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

        Screen.SCRAPER_SETTINGS -> ScraperSettingsScreen(
            apiKey = uiState.scraperApiKey,
            status = uiState.scraperStatus,
            running = uiState.scraperRunning,
            selectedIndex = uiState.menuIndex,
            onApiKeyChanged = viewModel::setTheGamesDbApiKey,
            onScrapeNewMissing = { viewModel.scrapeGbaLibrary(ScrapeMode.MISSING) },
            onScrapeMissingArtwork = { viewModel.scrapeGbaLibrary(ScrapeMode.MISSING_ARTWORK) },
            onScrapeMissingMetadata = { viewModel.scrapeGbaLibrary(ScrapeMode.MISSING_METADATA) },
            onRescrapeAll = { viewModel.scrapeGbaLibrary(ScrapeMode.RESCRAPE_ALL) },
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
            menuOpen = uiState.emulationMenuOpen,
            menuPage = uiState.emulationMenuPage,
            menuIndex = uiState.emulationMenuIndex,
            menuStatus = uiState.emulationMenuStatus,
            scaleMode = uiState.videoScaleMode,
            filterMode = uiState.videoFilterMode,
            menuHotkey = uiState.menuHotkey,
            selectedStateSlot = uiState.selectedStateSlot,
            selectedStateSummary = uiState.selectedStateSummary,
            onScreenMenuIconEnabled = uiState.onScreenMenuIconEnabled,
            onMenuIconClick = viewModel::toggleInGameMenuFromTouch,
            onMenuItemClick = viewModel::onEmulationMenuTouch,
            onStateSlotChange = viewModel::onStateSlotTouch,
        )

        Screen.INPUT_TEST -> InputTestScreen(
            events = uiState.inputEvents,
            currentlyHeld = uiState.currentlyHeld,
        )
    }
}
