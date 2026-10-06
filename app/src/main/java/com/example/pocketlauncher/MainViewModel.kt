package com.example.pocketlauncher

import android.app.Application
import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketlauncher.engine.PocketEngine
import com.example.pocketlauncher.engine.CoreDownloadManager
import com.example.pocketlauncher.engine.RomRuntimeStager
import com.example.pocketlauncher.engine.EngineAudioPlayer
import com.example.pocketlauncher.engine.BatterySaveManager
import com.example.pocketlauncher.engine.SaveStateManager
import com.example.pocketlauncher.engine.EmulationPreferencesStore
import com.example.pocketlauncher.engine.MenuHotkey
import com.example.pocketlauncher.engine.VideoFilterMode
import com.example.pocketlauncher.engine.VideoScaleMode
import com.example.pocketlauncher.input.PocketButton
import com.example.pocketlauncher.input.PocketInputMapper
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.library.RomFolderStore
import com.example.pocketlauncher.library.RomScanner
import com.example.pocketlauncher.library.SystemLibraryStore
import com.example.pocketlauncher.scraper.ScrapeCache
import com.example.pocketlauncher.scraper.ScrapedGameData
import com.example.pocketlauncher.scraper.ScraperPreferencesStore
import com.example.pocketlauncher.scraper.TheGamesDbClient
import com.example.pocketlauncher.ui.input.ButtonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.locks.LockSupport

enum class EmulationMenuPage {
    MAIN,
    DISPLAY,
    CONTROLLER,
}

enum class ScrapeMode {
    MISSING,
    MISSING_ARTWORK,
    MISSING_METADATA,
    RESCRAPE_ALL,
}

enum class Screen {
    HOME,
    PLATFORM,
    SETTINGS,
    FRONT_END_SETTINGS,
    SCRAPER_SETTINGS,
    EMULATOR_SETTINGS,
    SYSTEM_MANAGER,
    CORE_DOWNLOADS,
    EMULATION,
    INPUT_TEST,
}

data class PocketUiState(
    val screen: Screen = Screen.HOME,
    val menuIndex: Int = 0,
    val engineReady: Boolean = false,
    val inputEvents: List<ButtonEvent> = emptyList(),
    val currentlyHeld: Set<PocketButton> = emptySet(),

    val enabledPlatforms: Set<Platform> = emptySet(),

    val selectedPlatform: Platform? = null,
    val games: List<GameEntry> = emptyList(),
    val gameIndex: Int = 0,
    val currentFolderUri: String? = null,
    val currentFolderLabel: String? = null,
    val isScanning: Boolean = false,

    val folderPickerRequested: Boolean = false,
    val systemSettingsRequested: Boolean = false,

    val scraperApiKey: String = "",
    val scraperStatus: String = "",
    val scraperRunning: Boolean = false,

    val coreInstalled: Boolean = false,
    val coreDownloading: Boolean = false,
    val coreStatus: String = "mGBA not installed",

    val emulationTitle: String = "",
    val emulationStatus: String = "",
    val emulationFrame: IntArray = IntArray(0),
    val emulationWidth: Int = 0,
    val emulationHeight: Int = 0,
    val emulationMenuOpen: Boolean = false,
    val emulationMenuPage: EmulationMenuPage = EmulationMenuPage.MAIN,
    val emulationMenuIndex: Int = 0,
    val emulationMenuStatus: String = "",
    val videoScaleMode: VideoScaleMode = VideoScaleMode.FIT,
    val videoFilterMode: VideoFilterMode = VideoFilterMode.SHARP,
    val menuHotkey: MenuHotkey = MenuHotkey.L3_R3,
    val selectedStateSlot: Int = 0,
    val selectedStateSummary: String = "Empty",
    val onScreenMenuIconEnabled: Boolean = true,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val folderStore = RomFolderStore(application)
    private val romScanner = RomScanner(application)
    private val systemStore = SystemLibraryStore(application)
    private val scraperPreferences = ScraperPreferencesStore(application)
    private val scrapeCache = ScrapeCache(application)
    private val theGamesDbClient = TheGamesDbClient()
    private val coreDownloadManager = CoreDownloadManager(application)
    private val romRuntimeStager = RomRuntimeStager(application)
    private val engineAudioPlayer = EngineAudioPlayer()
    private val batterySaveManager = BatterySaveManager(application)
    private val saveStateManager = SaveStateManager(application)
    private val emulationPreferences = EmulationPreferencesStore(application)
    private var emulationInputMask: Int = 0
    private val emulationHeldButtons = mutableSetOf<PocketButton>()
    private var activeSavePath: String? = null
    private var activeRomPath: String? = null
    private var activeGame: GameEntry? = null

    private val _uiState = MutableStateFlow(
        PocketUiState(
            engineReady = PocketEngine.getStatus()?.equals("READY", ignoreCase = true) == true,
            enabledPlatforms = systemStore.getEnabledPlatforms(),
            scraperApiKey = scraperPreferences.theGamesDbApiKey(),
            coreInstalled = coreDownloadManager.installedCorePath() != null,
            coreStatus = if (coreDownloadManager.installedCorePath() != null) {
                "mGBA installed"
            } else {
                "mGBA not installed"
            },
            videoScaleMode = emulationPreferences.scaleMode(),
            videoFilterMode = emulationPreferences.filterMode(),
            menuHotkey = emulationPreferences.menuHotkey(),
            onScreenMenuIconEnabled = emulationPreferences.onScreenMenuIconEnabled(),
        )
    )
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    fun onKeyEvent(event: KeyEvent, pressed: Boolean): Boolean {
        val button = PocketInputMapper.map(event) ?: return false

        if (_uiState.value.screen == Screen.INPUT_TEST) {
            recordInputEvent(button, pressed)
        }

        if (_uiState.value.screen == Screen.EMULATION) {
            return handleEmulationInput(button, pressed)
        }

        if (!pressed) return true

        return when (_uiState.value.screen) {
            Screen.HOME -> handleHomeInput(button)
            Screen.PLATFORM -> handlePlatformInput(button)
            Screen.SETTINGS -> handleSettingsInput(button)
            Screen.FRONT_END_SETTINGS -> handleFrontEndSettingsInput(button)
            Screen.SCRAPER_SETTINGS -> handleScraperSettingsInput(button)
            Screen.EMULATOR_SETTINGS -> handleEmulatorSettingsInput(button)
            Screen.SYSTEM_MANAGER -> handleSystemManagerInput(button)
            Screen.CORE_DOWNLOADS -> handleCoreDownloadsInput(button)
            Screen.EMULATION -> true
            Screen.INPUT_TEST -> handleInputTestInput(button)
        }
    }

    private fun enabledPlatformsInOrder(): List<Platform> =
        Platform.entries.filter { it in _uiState.value.enabledPlatforms }

    private fun moveMenu(button: PocketButton, size: Int): Boolean {
        if (size <= 0) return false

        return when (button) {
            PocketButton.UP -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex - 1 + size) % size)
                }
                true
            }
            PocketButton.DOWN -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex + 1) % size)
                }
                true
            }
            else -> false
        }
    }

    private fun handleHomeInput(button: PocketButton): Boolean {
        val platforms = enabledPlatformsInOrder()
        val menuSize = platforms.size + 2

        when (button) {
            PocketButton.LEFT, PocketButton.UP -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex - 1 + menuSize) % menuSize)
                }
                return true
            }
            PocketButton.RIGHT, PocketButton.DOWN -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex + 1) % menuSize)
                }
                return true
            }
            else -> Unit
        }

        return when (button) {
            PocketButton.A -> {
                when {
                    _uiState.value.menuIndex == 0 -> Unit
                    _uiState.value.menuIndex == menuSize - 1 -> {
                        _uiState.update {
                            it.copy(
                                screen = Screen.SETTINGS,
                                menuIndex = 0,
                            )
                        }
                    }
                    else -> {
                        val platform = platforms.getOrNull(_uiState.value.menuIndex - 1)
                        if (platform != null) openPlatform(platform)
                    }
                }
                true
            }
            else -> false
        }
    }

    private fun handleSettingsInput(button: PocketButton): Boolean {
        if (moveMenu(button, 4)) return true

        return when (button) {
            PocketButton.A -> {
                when (_uiState.value.menuIndex) {
                    0 -> _uiState.update {
                        it.copy(screen = Screen.FRONT_END_SETTINGS, menuIndex = 0)
                    }
                    1 -> _uiState.update {
                        it.copy(screen = Screen.EMULATOR_SETTINGS, menuIndex = 0)
                    }
                    2 -> _uiState.update {
                        it.copy(systemSettingsRequested = true)
                    }
                }
                true
            }
            PocketButton.B -> {
                _uiState.update { it.copy(screen = Screen.HOME, menuIndex = 0) }
                true
            }
            else -> false
        }
    }

    private fun handleFrontEndSettingsInput(button: PocketButton): Boolean {
        if (moveMenu(button, 3)) return true

        return when (button) {
            PocketButton.A -> {
                when (_uiState.value.menuIndex) {
                    1 -> _uiState.update {
                        it.copy(screen = Screen.SCRAPER_SETTINGS, menuIndex = 0)
                    }
                    2 -> _uiState.update {
                        it.copy(screen = Screen.INPUT_TEST, menuIndex = 0)
                    }
                }
                true
            }
            PocketButton.B -> {
                _uiState.update { it.copy(screen = Screen.SETTINGS, menuIndex = 0) }
                true
            }
            else -> false
        }
    }

    private fun handleScraperSettingsInput(button: PocketButton): Boolean {
        if (moveMenu(button, 3)) return true

        return when (button) {
            PocketButton.A -> {
                when (_uiState.value.menuIndex) {
                    0 -> scrapeGbaLibrary(ScrapeMode.MISSING)
                    1 -> scrapeGbaLibrary(ScrapeMode.MISSING_ARTWORK)
                    2 -> scrapeGbaLibrary(ScrapeMode.MISSING_METADATA)
                    3 -> scrapeGbaLibrary(ScrapeMode.RESCRAPE_ALL)
                }
                true
            }
            PocketButton.B -> {
                _uiState.update {
                    it.copy(screen = Screen.FRONT_END_SETTINGS, menuIndex = 1)
                }
                true
            }
            else -> true
        }
    }

    fun setTheGamesDbApiKey(value: String) {
        scraperPreferences.setTheGamesDbApiKey(value)
        _uiState.update {
            it.copy(
                scraperApiKey = value,
                scraperStatus = if (value.isBlank()) "API KEY CLEARED" else "API KEY SAVED",
            )
        }
    }

    private fun handleEmulatorSettingsInput(button: PocketButton): Boolean {
        if (moveMenu(button, 3)) return true

        return when (button) {
            PocketButton.A -> {
                when (_uiState.value.menuIndex) {
                    0 -> _uiState.update {
                        it.copy(screen = Screen.SYSTEM_MANAGER, menuIndex = 0)
                    }
                    1 -> _uiState.update {
                        it.copy(screen = Screen.CORE_DOWNLOADS, menuIndex = 0)
                    }
                }
                true
            }
            PocketButton.B -> {
                _uiState.update { it.copy(screen = Screen.SETTINGS, menuIndex = 1) }
                true
            }
            else -> false
        }
    }

    private fun handleCoreDownloadsInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.A -> {
                if (!_uiState.value.coreDownloading) {
                    downloadAndLoadMgba()
                }
                true
            }
            PocketButton.X -> {
                PocketEngine.unloadCore()
                coreDownloadManager.removeMgba()
                _uiState.update {
                    it.copy(
                        coreInstalled = false,
                        coreStatus = "mGBA not installed",
                    )
                }
                true
            }
            PocketButton.B -> {
                _uiState.update {
                    it.copy(screen = Screen.EMULATOR_SETTINGS, menuIndex = 1)
                }
                true
            }
            else -> false
        }
    }

    private fun downloadAndLoadMgba() {
        _uiState.update {
            it.copy(
                coreDownloading = true,
                coreStatus = "Downloading mGBA...",
            )
        }

        viewModelScope.launch {
            val path = coreDownloadManager.installedCorePath()
                ?: coreDownloadManager.installMgba().getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            coreDownloading = false,
                            coreInstalled = false,
                            coreStatus = "Download failed: ${error.message ?: "Unknown error"}",
                        )
                    }
                    return@launch
                }.path

            val result = PocketEngine.loadCore(path)
            result.onSuccess { info ->
                _uiState.update {
                    it.copy(
                        coreDownloading = false,
                        coreInstalled = true,
                        coreStatus = buildString {
                            append(info.name.ifBlank { "mGBA" })
                            if (info.version.isNotBlank()) append("  ").append(info.version)
                            if (info.extensions.isNotBlank()) append("  ·  ").append(info.extensions)
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        coreDownloading = false,
                        coreInstalled = true,
                        coreStatus = "Installed, but load failed: ${error.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleSystemManagerInput(button: PocketButton): Boolean {
        if (moveMenu(button, Platform.entries.size)) return true

        return when (button) {
            PocketButton.A -> {
                val platform = Platform.entries.getOrNull(_uiState.value.menuIndex)
                    ?: return true
                val currentlyEnabled = platform in _uiState.value.enabledPlatforms
                val updated = systemStore.setEnabled(platform, !currentlyEnabled)
                _uiState.update { it.copy(enabledPlatforms = updated) }
                true
            }
            PocketButton.B -> {
                _uiState.update {
                    it.copy(screen = Screen.EMULATOR_SETTINGS, menuIndex = 0)
                }
                true
            }
            else -> false
        }
    }

    private fun handlePlatformInput(button: PocketButton): Boolean {
        val state = _uiState.value

        return when (button) {
            PocketButton.LEFT, PocketButton.UP -> {
                if (state.games.isNotEmpty()) {
                    _uiState.update {
                        it.copy(gameIndex = (it.gameIndex - 1 + it.games.size) % it.games.size)
                    }
                }
                true
            }
            PocketButton.RIGHT, PocketButton.DOWN -> {
                if (state.games.isNotEmpty()) {
                    _uiState.update {
                        it.copy(gameIndex = (it.gameIndex + 1) % it.games.size)
                    }
                }
                true
            }
            PocketButton.A -> {
                if (state.currentFolderUri == null) {
                    requestFolderPicker()
                } else {
                    val game = state.games.getOrNull(state.gameIndex)
                    if (game != null && game.platform == Platform.GBA) {
                        startGame(game)
                    }
                }
                true
            }
            PocketButton.X -> {
                requestFolderPicker()
                true
            }
            PocketButton.Y -> {
                rescanCurrentPlatform()
                true
            }
            PocketButton.B -> {
                _uiState.update {
                    it.copy(
                        screen = Screen.HOME,
                        menuIndex = 0,
                        selectedPlatform = null,
                        games = emptyList(),
                        gameIndex = 0,
                        currentFolderUri = null,
                        currentFolderLabel = null,
                        isScanning = false,
                        folderPickerRequested = false,
                    )
                }
                true
            }
            else -> false
        }
    }

    private fun handleEmulationInput(button: PocketButton, pressed: Boolean): Boolean {
        if (pressed) {
            emulationHeldButtons += button
        } else {
            emulationHeldButtons -= button
        }

        if (pressed && isMenuHotkeyHeld()) {
            emulationInputMask = 0
            PocketEngine.setInputMask(0)
            emulationHeldButtons.clear()
            _uiState.update {
                it.copy(
                    emulationMenuOpen = !it.emulationMenuOpen,
                    emulationMenuPage = EmulationMenuPage.MAIN,
                    emulationMenuIndex = 0,
                    emulationMenuStatus = "",
                )
            }
            return true
        }

        if (_uiState.value.emulationMenuOpen) {
            if (!pressed) return true
            val state = _uiState.value
            val itemCount = when (state.emulationMenuPage) {
                EmulationMenuPage.MAIN -> 8
                EmulationMenuPage.DISPLAY -> 3
                EmulationMenuPage.CONTROLLER -> 3
            }

            return when (button) {
                PocketButton.UP -> {
                    _uiState.update {
                        it.copy(
                            emulationMenuIndex = (it.emulationMenuIndex - 1 + itemCount) % itemCount,
                            emulationMenuStatus = "",
                        )
                    }
                    true
                }
                PocketButton.DOWN -> {
                    _uiState.update {
                        it.copy(
                            emulationMenuIndex = (it.emulationMenuIndex + 1) % itemCount,
                            emulationMenuStatus = "",
                        )
                    }
                    true
                }
                PocketButton.LEFT -> {
                    if (state.emulationMenuPage == EmulationMenuPage.MAIN && state.emulationMenuIndex == 3) {
                        cycleStateSlot(-1)
                    } else {
                        activateEmulationMenuItem()
                    }
                    true
                }
                PocketButton.RIGHT -> {
                    if (state.emulationMenuPage == EmulationMenuPage.MAIN && state.emulationMenuIndex == 3) {
                        cycleStateSlot(1)
                    } else {
                        activateEmulationMenuItem()
                    }
                    true
                }
                PocketButton.A -> {
                    if (!(state.emulationMenuPage == EmulationMenuPage.MAIN && state.emulationMenuIndex == 3)) {
                        activateEmulationMenuItem()
                    }
                    true
                }
                PocketButton.B -> {
                    if (state.emulationMenuPage == EmulationMenuPage.MAIN) {
                        _uiState.update {
                            it.copy(
                                emulationMenuOpen = false,
                                emulationMenuStatus = "",
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                emulationMenuPage = EmulationMenuPage.MAIN,
                                emulationMenuIndex = 0,
                                emulationMenuStatus = "",
                            )
                        }
                    }
                    true
                }
                else -> true
            }
        }

        val bit = when (button) {
            PocketButton.B -> 0
            PocketButton.Y -> 1
            PocketButton.SELECT -> 2
            PocketButton.START -> 3
            PocketButton.UP -> 4
            PocketButton.DOWN -> 5
            PocketButton.LEFT -> 6
            PocketButton.RIGHT -> 7
            PocketButton.A -> 8
            PocketButton.X -> 9
            PocketButton.L1 -> 10
            PocketButton.R1 -> 11
            else -> null
        }

        if (bit != null) {
            emulationInputMask = if (pressed) {
                emulationInputMask or (1 shl bit)
            } else {
                emulationInputMask and (1 shl bit).inv()
            }
            PocketEngine.setInputMask(emulationInputMask)
        }

        return true
    }

    private fun isMenuHotkeyHeld(): Boolean = when (_uiState.value.menuHotkey) {
        MenuHotkey.L3_R3 ->
            PocketButton.L3 in emulationHeldButtons && PocketButton.R3 in emulationHeldButtons
        MenuHotkey.START_SELECT ->
            PocketButton.START in emulationHeldButtons && PocketButton.SELECT in emulationHeldButtons
        MenuHotkey.L1_R1 ->
            PocketButton.L1 in emulationHeldButtons && PocketButton.R1 in emulationHeldButtons
    }

    private fun activateEmulationMenuItem() {
        val state = _uiState.value
        when (state.emulationMenuPage) {
            EmulationMenuPage.MAIN -> when (state.emulationMenuIndex) {
                0 -> _uiState.update { it.copy(emulationMenuOpen = false, emulationMenuStatus = "") }
                1 -> saveStateSlot()
                2 -> loadStateSlot()
                3 -> Unit
                4 -> _uiState.update {
                    it.copy(
                        emulationMenuPage = EmulationMenuPage.DISPLAY,
                        emulationMenuIndex = 0,
                        emulationMenuStatus = "",
                    )
                }
                5 -> _uiState.update {
                    it.copy(
                        emulationMenuPage = EmulationMenuPage.CONTROLLER,
                        emulationMenuIndex = 0,
                        emulationMenuStatus = "",
                    )
                }
                6 -> restartGame()
                7 -> stopGame()
            }
            EmulationMenuPage.DISPLAY -> when (state.emulationMenuIndex) {
                0 -> {
                    val next = when (state.videoScaleMode) {
                        VideoScaleMode.FIT -> VideoScaleMode.INTEGER
                        VideoScaleMode.INTEGER -> VideoScaleMode.STRETCH
                        VideoScaleMode.STRETCH -> VideoScaleMode.FIT
                    }
                    emulationPreferences.setScaleMode(next)
                    _uiState.update { it.copy(videoScaleMode = next) }
                }
                1 -> {
                    val next = if (state.videoFilterMode == VideoFilterMode.SHARP) {
                        VideoFilterMode.SMOOTH
                    } else {
                        VideoFilterMode.SHARP
                    }
                    emulationPreferences.setFilterMode(next)
                    _uiState.update { it.copy(videoFilterMode = next) }
                }
                2 -> _uiState.update {
                    it.copy(
                        emulationMenuPage = EmulationMenuPage.MAIN,
                        emulationMenuIndex = 4,
                        emulationMenuStatus = "",
                    )
                }
            }
            EmulationMenuPage.CONTROLLER -> when (state.emulationMenuIndex) {
                0 -> {
                    val next = when (state.menuHotkey) {
                        MenuHotkey.L3_R3 -> MenuHotkey.START_SELECT
                        MenuHotkey.START_SELECT -> MenuHotkey.L1_R1
                        MenuHotkey.L1_R1 -> MenuHotkey.L3_R3
                    }
                    emulationPreferences.setMenuHotkey(next)
                    _uiState.update {
                        it.copy(
                            menuHotkey = next,
                            emulationMenuStatus = "Menu shortcut updated",
                        )
                    }
                }
                1 -> {
                    val next = !state.onScreenMenuIconEnabled
                    emulationPreferences.setOnScreenMenuIconEnabled(next)
                    _uiState.update {
                        it.copy(
                            onScreenMenuIconEnabled = next,
                            emulationMenuStatus = if (next) "On-screen menu icon enabled" else "On-screen menu icon disabled",
                        )
                    }
                }
                2 -> _uiState.update {
                    it.copy(
                        emulationMenuPage = EmulationMenuPage.MAIN,
                        emulationMenuIndex = 5,
                        emulationMenuStatus = "",
                    )
                }
            }
        }
    }

    private fun saveStateSlot() {
        val game = activeGame
        if (game == null) {
            _uiState.update { it.copy(emulationMenuStatus = "No active game") }
            return
        }

        val slot = _uiState.value.selectedStateSlot
        val path = saveStateManager.slotFile(game, slot).absolutePath
        activeSavePath?.let { PocketEngine.saveSaveRam(it) }
        val saved = PocketEngine.saveState(path)
        val summary = saveStateManager.slotSummary(game, slot)
        _uiState.update {
            it.copy(
                selectedStateSummary = summary,
                emulationMenuStatus = if (saved) "State saved · Slot ${slot + 1}" else "Save state failed",
            )
        }
    }

    private fun loadStateSlot() {
        val game = activeGame
        if (game == null) {
            _uiState.update { it.copy(emulationMenuStatus = "No active game") }
            return
        }

        val slot = _uiState.value.selectedStateSlot
        val path = saveStateManager.slotFile(game, slot).absolutePath
        val loaded = PocketEngine.loadState(path)
        _uiState.update {
            it.copy(
                emulationMenuStatus = if (loaded) "State loaded · Slot ${slot + 1}" else "Slot ${slot + 1} is empty",
            )
        }
    }

    private fun cycleStateSlot(delta: Int) {
        val game = activeGame ?: return
        val current = _uiState.value.selectedStateSlot
        val next = (current + delta + 3) % 3
        _uiState.update {
            it.copy(
                selectedStateSlot = next,
                selectedStateSummary = saveStateManager.slotSummary(game, next),
                emulationMenuStatus = "",
            )
        }
    }
    private fun restartGame() {
        val romPath = activeRomPath
        if (romPath == null) {
            _uiState.update { it.copy(emulationMenuStatus = "No active game") }
            return
        }

        activeSavePath?.let { PocketEngine.saveSaveRam(it) }
        engineAudioPlayer.stop()
        PocketEngine.unloadGame()

        val restarted = PocketEngine.loadGame(romPath).isSuccess
        if (restarted) {
            activeSavePath?.let { PocketEngine.loadSaveRam(it) }
            emulationInputMask = 0
            PocketEngine.setInputMask(0)
            engineAudioPlayer.start(PocketEngine.audioSampleRate())
            _uiState.update {
                it.copy(
                    emulationMenuOpen = false,
                    emulationMenuPage = EmulationMenuPage.MAIN,
                    emulationMenuIndex = 0,
                    emulationMenuStatus = "",
                )
            }
        } else {
            _uiState.update { it.copy(emulationMenuStatus = "Restart failed") }
        }
    }

    private fun startGame(game: GameEntry) {
        if (_uiState.value.screen == Screen.EMULATION) return

        _uiState.update {
            it.copy(
                emulationTitle = game.displayName,
                emulationStatus = "Preparing ROM...",
                emulationFrame = IntArray(0),
                emulationWidth = 0,
                emulationHeight = 0,
                emulationMenuOpen = false,
                emulationMenuIndex = 0,
            )
        }

        viewModelScope.launch {
            val corePath = coreDownloadManager.installedCorePath()
            if (corePath == null) {
                _uiState.update {
                    it.copy(emulationStatus = "mGBA is not installed. Install it in Emulator Settings → Core Downloads.")
                }
                return@launch
            }

            val staged = romRuntimeStager.stage(game.uri, game.fileName).getOrElse { error ->
                _uiState.update {
                    it.copy(emulationStatus = "ROM staging failed: ${error.message ?: "Unknown error"}")
                }
                return@launch
            }

            PocketEngine.unloadGame()
            PocketEngine.unloadCore()

            PocketEngine.loadCore(corePath).getOrElse { error ->
                _uiState.update {
                    it.copy(emulationStatus = "Core load failed: ${error.message ?: "Unknown error"}")
                }
                return@launch
            }

            PocketEngine.loadGame(staged.absolutePath).getOrElse { error ->
                _uiState.update {
                    it.copy(emulationStatus = "Game load failed: ${error.message ?: "Unknown error"}")
                }
                return@launch
            }

            activeGame = game
            activeRomPath = staged.absolutePath
            activeSavePath = batterySaveManager.saveFile(game).absolutePath
            activeSavePath?.let { PocketEngine.loadSaveRam(it) }

            emulationInputMask = 0
            PocketEngine.setInputMask(0)
            engineAudioPlayer.start(PocketEngine.audioSampleRate())

            _uiState.update {
                it.copy(
                    screen = Screen.EMULATION,
                    emulationStatus = "Starting ${game.displayName}...",
                    emulationMenuOpen = false,
                    emulationMenuPage = EmulationMenuPage.MAIN,
                    emulationMenuIndex = 0,
                    emulationMenuStatus = "",
                    selectedStateSlot = 0,
                    selectedStateSummary = saveStateManager.slotSummary(game, 0),
                )
            }

            launchEmulationLoop()
        }
    }

    private fun launchEmulationLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            val fps = PocketEngine.videoFps().coerceIn(30.0, 240.0)
            val frameDurationNanos = (1_000_000_000.0 / fps).toLong()
            var nextFrameDeadline = System.nanoTime()
            var nextSaveFlush = System.nanoTime() + 5_000_000_000L

            while (isActive && _uiState.value.screen == Screen.EMULATION) {
                if (_uiState.value.emulationMenuOpen) {
                    LockSupport.parkNanos(5_000_000L)
                    nextFrameDeadline = System.nanoTime()
                    continue
                }

                val now = System.nanoTime()
                val waitNanos = nextFrameDeadline - now
                if (waitNanos > 0) {
                    LockSupport.parkNanos(waitNanos)
                } else if (waitNanos < -frameDurationNanos * 4) {
                    nextFrameDeadline = System.nanoTime()
                }

                if (!PocketEngine.runFrame()) {
                    _uiState.update {
                        it.copy(emulationStatus = "Emulation stopped unexpectedly.")
                    }
                    break
                }

                val audio = PocketEngine.drainAudio()
                if (audio.isNotEmpty()) {
                    engineAudioPlayer.enqueue(audio)
                }

                val saveNow = System.nanoTime()
                if (saveNow >= nextSaveFlush) {
                    activeSavePath?.let { PocketEngine.saveSaveRam(it) }
                    nextSaveFlush = saveNow + 5_000_000_000L
                }

                nextFrameDeadline += frameDurationNanos
            }
        }
    }

    private fun stopGame() {
        emulationInputMask = 0
        PocketEngine.setInputMask(0)
        emulationHeldButtons.clear()
        activeSavePath?.let { PocketEngine.saveSaveRam(it) }
        activeSavePath = null
        activeRomPath = null
        activeGame = null
        engineAudioPlayer.stop()
        PocketEngine.unloadGame()
        romRuntimeStager.clear()
        _uiState.update {
            it.copy(
                screen = Screen.PLATFORM,
                emulationStatus = "",
                emulationFrame = IntArray(0),
                emulationWidth = 0,
                emulationHeight = 0,
                emulationMenuOpen = false,
                emulationMenuPage = EmulationMenuPage.MAIN,
                emulationMenuIndex = 0,
                emulationMenuStatus = "",
            )
        }
    }

    fun toggleInGameMenuFromTouch() {
        if (_uiState.value.screen != Screen.EMULATION) return
        emulationInputMask = 0
        PocketEngine.setInputMask(0)
        emulationHeldButtons.clear()
        _uiState.update {
            it.copy(
                emulationMenuOpen = !it.emulationMenuOpen,
                emulationMenuPage = EmulationMenuPage.MAIN,
                emulationMenuIndex = 0,
                emulationMenuStatus = "",
            )
        }
    }

    fun onEmulationMenuTouch(index: Int) {
        if (_uiState.value.screen != Screen.EMULATION || !_uiState.value.emulationMenuOpen) return
        _uiState.update { it.copy(emulationMenuIndex = index, emulationMenuStatus = "") }

        val state = _uiState.value
        if (state.emulationMenuPage == EmulationMenuPage.MAIN && index == 3) {
            return
        }
        activateEmulationMenuItem()
    }

    fun onStateSlotTouch(delta: Int) {
        if (_uiState.value.screen != Screen.EMULATION || !_uiState.value.emulationMenuOpen) return
        if (_uiState.value.emulationMenuPage != EmulationMenuPage.MAIN) return
        _uiState.update { it.copy(emulationMenuIndex = 3, emulationMenuStatus = "") }
        cycleStateSlot(delta)
    }

    private fun handleInputTestInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.B -> {
                _uiState.update {
                    it.copy(screen = Screen.FRONT_END_SETTINGS, menuIndex = 2)
                }
                true
            }
            else -> false
        }
    }

    private fun openPlatform(platform: Platform) {
        val folderUri = folderStore.getFolderUri(platform)
        val folderLabel = folderStore.getFolderLabel(folderUri)
            ?: if (folderUri != null) "ROM folder" else null

        _uiState.update {
            it.copy(
                screen = Screen.PLATFORM,
                selectedPlatform = platform,
                games = emptyList(),
                gameIndex = 0,
                currentFolderUri = folderUri,
                currentFolderLabel = folderLabel,
                isScanning = folderUri != null,
                folderPickerRequested = false,
            )
        }

        if (folderUri != null) {
            scanPlatform(platform, folderUri)
        }
    }

    private fun requestFolderPicker() {
        if (_uiState.value.selectedPlatform == null) return
        _uiState.update { it.copy(folderPickerRequested = true) }
    }

    fun onFolderPickerLaunched() {
        _uiState.update { it.copy(folderPickerRequested = false) }
    }

    fun onFolderSelected(uri: Uri) {
        val platform = _uiState.value.selectedPlatform ?: return

        folderStore.setFolderUri(platform, uri)

        val uriString = uri.toString()
        _uiState.update {
            it.copy(
                currentFolderUri = uriString,
                currentFolderLabel = folderStore.getFolderLabel(uriString) ?: "ROM folder",
                games = emptyList(),
                gameIndex = 0,
                isScanning = true,
            )
        }

        scanPlatform(platform, uriString)
    }

    fun onSystemSettingsLaunched() {
        _uiState.update { it.copy(systemSettingsRequested = false) }
    }

    private fun rescanCurrentPlatform() {
        val state = _uiState.value
        val platform = state.selectedPlatform ?: return
        val folderUri = state.currentFolderUri ?: return

        _uiState.update { it.copy(isScanning = true) }
        scanPlatform(platform, folderUri)
    }

    private fun scanPlatform(platform: Platform, folderUri: String) {
        viewModelScope.launch {
            val scannedGames = romScanner.scan(platform, folderUri)
            val games = scannedGames.map { game ->
                scrapeCache.get(game)?.let { cached -> applyScrapedData(game, cached) } ?: game
            }

            if (_uiState.value.screen != Screen.PLATFORM ||
                _uiState.value.selectedPlatform != platform
            ) {
                return@launch
            }

            _uiState.update {
                it.copy(
                    games = games,
                    gameIndex = 0,
                    isScanning = false,
                )
            }

        }
    }

    fun scrapeGbaLibrary(mode: ScrapeMode) {
        if (_uiState.value.scraperRunning) return

        val apiKey = _uiState.value.scraperApiKey.trim()
        if (apiKey.isBlank()) {
            _uiState.update { it.copy(scraperStatus = "ADD A THEGAMESDB API KEY FIRST") }
            return
        }

        val folderUri = folderStore.getFolderUri(Platform.GBA)
        if (folderUri == null) {
            _uiState.update { it.copy(scraperStatus = "CONFIGURE A GBA ROM FOLDER FIRST") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    scraperRunning = true,
                    scraperStatus = "SCANNING GBA LIBRARY...",
                )
            }

            val games = romScanner.scan(Platform.GBA, folderUri)
            if (games.isEmpty()) {
                _uiState.update {
                    it.copy(
                        scraperRunning = false,
                        scraperStatus = "NO GBA ROMS FOUND",
                    )
                }
                return@launch
            }

            val targets = when (mode) {
                ScrapeMode.MISSING -> games.filter { scrapeCache.get(it) == null }
                ScrapeMode.MISSING_ARTWORK -> games.filter { game ->
                    val cached = scrapeCache.get(game)
                    cached == null || cached.artworkUrl.isNullOrBlank()
                }
                ScrapeMode.MISSING_METADATA -> games.filter { game ->
                    val cached = scrapeCache.get(game)
                    cached == null || cached.releaseDate.isNullOrBlank() || cached.rating.isNullOrBlank()
                }
                ScrapeMode.RESCRAPE_ALL -> games
            }

            if (targets.isEmpty()) {
                _uiState.update {
                    it.copy(
                        scraperRunning = false,
                        scraperStatus = when (mode) {
                            ScrapeMode.MISSING -> "NO NEW OR UNSCRAPED GAMES"
                            ScrapeMode.MISSING_ARTWORK -> "NO GAMES WITH MISSING ARTWORK"
                            ScrapeMode.MISSING_METADATA -> "NO GAMES WITH MISSING METADATA"
                            ScrapeMode.RESCRAPE_ALL -> "NOTHING TO RESCRAPE"
                        },
                    )
                }
                return@launch
            }

            val cappedTargets = targets.take(50)
            var completed = 0
            var failed = 0

            for ((index, game) in cappedTargets.withIndex()) {
                _uiState.update {
                    it.copy(scraperStatus = "${index + 1} / ${cappedTargets.size}  ·  ${game.displayName}")
                }

                val result = theGamesDbClient.scrape(game, apiKey)
                val data = result.getOrNull()
                if (data != null) {
                    scrapeCache.put(game, data)
                    completed += 1

                    _uiState.update { state ->
                        val updatedGames = if (state.selectedPlatform == Platform.GBA) {
                            state.games.map { existing ->
                                if (existing.uri == game.uri) applyScrapedData(existing, data) else existing
                            }
                        } else {
                            state.games
                        }
                        state.copy(games = updatedGames)
                    }
                } else {
                    failed += 1
                }
            }

            _uiState.update {
                it.copy(
                    scraperRunning = false,
                    scraperStatus = buildString {
                        append("SCRAPED ").append(completed)
                        if (failed > 0) append("  ·  ").append(failed).append(" FAILED")
                        if (targets.size > cappedTargets.size) {
                            append("  ·  ").append(targets.size - cappedTargets.size).append(" REMAINING")
                        }
                    },
                )
            }
        }
    }
    private fun applyScrapedData(game: GameEntry, data: ScrapedGameData): GameEntry =
        game.copy(
            displayName = data.title ?: game.displayName,
            artworkUrl = data.artworkUrl,
            developer = data.developer,
            publisher = data.publisher,
            releaseDate = data.releaseDate,
            genre = data.genre,
            rating = data.rating,
            scrapeProvider = data.provider,
        )

    private fun recordInputEvent(button: PocketButton, pressed: Boolean) {
        _uiState.update { state ->
            val newHeld = if (pressed) {
                state.currentlyHeld + button
            } else {
                state.currentlyHeld - button
            }

            val newEvents = (state.inputEvents + ButtonEvent(button, pressed))
                .takeLast(100)

            state.copy(
                currentlyHeld = newHeld,
                inputEvents = newEvents,
            )
        }
    }

    fun onEngineReady() {
        _uiState.update { it.copy(engineReady = true) }
    }
}
