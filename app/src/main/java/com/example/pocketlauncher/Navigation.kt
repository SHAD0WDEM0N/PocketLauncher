package com.example.pocketlauncher

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.ui.home.HomeScreen
import com.example.pocketlauncher.ui.emulation.EmulationScreen
import com.example.pocketlauncher.ui.input.InputTestScreen
import com.example.pocketlauncher.ui.platform.PlatformScreen
import com.example.pocketlauncher.ui.platform.RecentlyPlayedScreen
import com.example.pocketlauncher.ui.platform.GameOptionsScreen
import com.example.pocketlauncher.ui.platform.ScrapeMatchScreen
import com.example.pocketlauncher.ui.settings.EmulatorSettingsScreen
import com.example.pocketlauncher.ui.settings.CoreDownloadsScreen
import com.example.pocketlauncher.ui.settings.FrontEndSettingsScreen
import com.example.pocketlauncher.ui.settings.SettingsScreen
import com.example.pocketlauncher.ui.settings.ScraperSettingsScreen
import com.example.pocketlauncher.ui.settings.SystemManagerScreen
import com.example.pocketlauncher.ui.settings.ThemeSettingsScreen

@Composable
fun MainNavigation(
    uiState: PocketUiState,
    viewModel: MainViewModel,
) {
    when (uiState.screen) {
        Screen.HOME -> {
            val menuItems = buildList {
                add("Recently Played")
                if (uiState.hasFavourites) add("Favourites")
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
                touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
                onItemClick = viewModel::onHomeItemTouch,
                onIndexChange = viewModel::onHomeIndexTouch,
                onSelect = viewModel::onHomeSelectedTouch,
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
                    touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
                    onGameClick = viewModel::onGameTouch,
                    onIndexChange = viewModel::onGameIndexTouch,
                    onPlay = viewModel::onSelectedGamePlayTouch,
                    onOptions = viewModel::onSelectedGameOptionsTouch,
                    onFavourite = viewModel::onSelectedGameFavouriteTouch,
                    onBack = viewModel::onTouchBack,
                )
            }
        }

        Screen.RECENTLY_PLAYED -> RecentlyPlayedScreen(
            games = uiState.games,
            selectedIndex = uiState.gameIndex,
            isScanning = uiState.isScanning,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onGameClick = viewModel::onGameTouch,
            onIndexChange = viewModel::onGameIndexTouch,
            onPlay = viewModel::onSelectedGamePlayTouch,
            onOptions = viewModel::onSelectedGameOptionsTouch,
            onFavourite = viewModel::onSelectedGameFavouriteTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.FAVOURITES -> RecentlyPlayedScreen(
            games = uiState.games,
            selectedIndex = uiState.gameIndex,
            isScanning = uiState.isScanning,
            title = "Favourites",
            emptyTitle = "No favourites yet.",
            emptySubtitle = "Favourite a game with Y to add it here.",
            showFavouriteAction = true,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onGameClick = viewModel::onGameTouch,
            onIndexChange = viewModel::onGameIndexTouch,
            onPlay = viewModel::onSelectedGamePlayTouch,
            onOptions = viewModel::onSelectedGameOptionsTouch,
            onFavourite = viewModel::onSelectedGameFavouriteTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.GAME_OPTIONS -> GameOptionsScreen(
            game = uiState.games.firstOrNull { it.uri == uiState.gameOptionsUri },
            selectedIndex = uiState.menuIndex,
            status = uiState.matchSearchStatus,
        )

        Screen.SCRAPE_MATCHES -> ScrapeMatchScreen(
            game = uiState.games.firstOrNull { it.uri == uiState.gameOptionsUri },
            candidates = uiState.scrapeCandidates,
            selectedIndex = uiState.scrapeCandidateIndex,
            status = uiState.matchSearchStatus,
            running = uiState.matchSearchRunning,
        )

        Screen.SETTINGS -> SettingsScreen(
            selectedIndex = uiState.menuIndex,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onItemClick = viewModel::onMenuItemTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.FRONT_END_SETTINGS -> FrontEndSettingsScreen(
            selectedIndex = uiState.menuIndex,
            themeSummary = "${uiState.themeMode.displayName} / ${uiState.themeAccent.displayName}",
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onItemClick = viewModel::onMenuItemTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.THEME_SETTINGS -> ThemeSettingsScreen(
            mode = uiState.themeMode,
            accent = uiState.themeAccent,
            background = uiState.backgroundStyle,
            selectedIndex = uiState.menuIndex,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onItemClick = viewModel::onThemeItemTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.SCRAPER_SETTINGS -> ScraperSettingsScreen(
            apiKey = uiState.scraperApiKey,
            status = uiState.scraperStatus,
            running = uiState.scraperRunning,
            selectedIndex = uiState.menuIndex,
            onApiKeyChanged = viewModel::setTheGamesDbApiKey,
            onScrapeNewMissing = { viewModel.scrapeHandheldLibraries(ScrapeMode.MISSING) },
            onScrapeMissingArtwork = { viewModel.scrapeHandheldLibraries(ScrapeMode.MISSING_ARTWORK) },
            onScrapeMissingMetadata = { viewModel.scrapeHandheldLibraries(ScrapeMode.MISSING_METADATA) },
            onRescrapeAll = { viewModel.scrapeHandheldLibraries(ScrapeMode.RESCRAPE_ALL) },
        )

        Screen.EMULATOR_SETTINGS -> EmulatorSettingsScreen(
            selectedIndex = uiState.menuIndex,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            enabledPlatforms = uiState.enabledPlatforms,
            onItemClick = viewModel::onMenuItemTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.SYSTEM_MANAGER -> SystemManagerScreen(
            selectedIndex = uiState.menuIndex,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            enabledPlatforms = uiState.enabledPlatforms,
            onItemClick = viewModel::onMenuItemTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.CORE_DOWNLOADS -> CoreDownloadsScreen(
            installed = uiState.coreInstalled,
            downloading = uiState.coreDownloading,
            status = uiState.coreStatus,
            touchMode = uiState.inputUiMode == InputUiMode.TOUCH,
            onItemClick = viewModel::onMenuItemTouch,
            onRemove = viewModel::onCoreRemoveTouch,
            onBack = viewModel::onTouchBack,
        )

        Screen.EMULATION -> EmulationScreen(
            menuOpen = uiState.emulationMenuOpen,
            menuPage = uiState.emulationMenuPage,
            menuIndex = uiState.emulationMenuIndex,
            menuStatus = uiState.emulationMenuStatus,
            scaleMode = uiState.videoScaleMode,
            filterMode = uiState.videoFilterMode,
            effectMode = uiState.videoEffectMode,
            borderMode = uiState.videoBorderMode,
            platformKey = uiState.emulationPlatformKey,
            menuHotkey = uiState.menuHotkey,
            selectedStateSlot = uiState.selectedStateSlot,
            selectedStateSummary = uiState.selectedStateSummary,
            selectedStateThumbnailPath = uiState.selectedStateThumbnailPath,
            onScreenMenuIconEnabled = uiState.onScreenMenuIconEnabled,
            onScreenControlsEnabled = uiState.onScreenControlsEnabled,
            inputUiMode = uiState.inputUiMode,
            onTouchUiInteraction = viewModel::onTouchUiInteraction,
            onMenuIconClick = viewModel::toggleInGameMenuFromTouch,
            onTouchControl = viewModel::onTouchControl,
            onMenuItemClick = viewModel::onEmulationMenuTouch,
            onStateSlotChange = viewModel::onStateSlotTouch,
        )

        Screen.INPUT_TEST -> InputTestScreen(
            events = uiState.inputEvents,
            currentlyHeld = uiState.currentlyHeld,
        )
    }
}
