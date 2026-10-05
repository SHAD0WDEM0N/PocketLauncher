package com.example.pocketlauncher

import android.app.Application
import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketlauncher.engine.PocketEngine
import com.example.pocketlauncher.engine.CoreDownloadManager
import com.example.pocketlauncher.engine.RomRuntimeStager
import com.example.pocketlauncher.input.PocketButton
import com.example.pocketlauncher.input.PocketInputMapper
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.library.RomFolderStore
import com.example.pocketlauncher.library.RomScanner
import com.example.pocketlauncher.library.SystemLibraryStore
import com.example.pocketlauncher.ui.input.ButtonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    PLATFORM,
    SETTINGS,
    FRONT_END_SETTINGS,
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

    val coreInstalled: Boolean = false,
    val coreDownloading: Boolean = false,
    val coreStatus: String = "mGBA not installed",

    val emulationTitle: String = "",
    val emulationStatus: String = "",
    val emulationFrame: IntArray = IntArray(0),
    val emulationWidth: Int = 0,
    val emulationHeight: Int = 0,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val folderStore = RomFolderStore(application)
    private val romScanner = RomScanner(application)
    private val systemStore = SystemLibraryStore(application)
    private val coreDownloadManager = CoreDownloadManager(application)
    private val romRuntimeStager = RomRuntimeStager(application)

    private val _uiState = MutableStateFlow(
        PocketUiState(
            engineReady = PocketEngine.getStatus()?.equals("READY", ignoreCase = true) == true,
            enabledPlatforms = systemStore.getEnabledPlatforms(),
            coreInstalled = coreDownloadManager.installedCorePath() != null,
            coreStatus = if (coreDownloadManager.installedCorePath() != null) {
                "mGBA installed"
            } else {
                "mGBA not installed"
            },
        )
    )
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    fun onKeyEvent(event: KeyEvent, pressed: Boolean): Boolean {
        val button = PocketInputMapper.map(event) ?: return false

        if (_uiState.value.screen == Screen.INPUT_TEST) {
            recordInputEvent(button, pressed)
        }

        if (!pressed) return true

        return when (_uiState.value.screen) {
            Screen.HOME -> handleHomeInput(button)
            Screen.PLATFORM -> handlePlatformInput(button)
            Screen.SETTINGS -> handleSettingsInput(button)
            Screen.FRONT_END_SETTINGS -> handleFrontEndSettingsInput(button)
            Screen.EMULATOR_SETTINGS -> handleEmulatorSettingsInput(button)
            Screen.SYSTEM_MANAGER -> handleSystemManagerInput(button)
            Screen.CORE_DOWNLOADS -> handleCoreDownloadsInput(button)
            Screen.EMULATION -> handleEmulationInput(button)
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

        if (moveMenu(button, menuSize)) return true

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
        if (moveMenu(button, 3)) return true

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
                if (_uiState.value.menuIndex == 2) {
                    _uiState.update {
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
            PocketButton.UP -> {
                if (state.games.isNotEmpty()) {
                    _uiState.update {
                        it.copy(gameIndex = (it.gameIndex - 1 + it.games.size) % it.games.size)
                    }
                }
                true
            }
            PocketButton.DOWN -> {
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

    private fun handleEmulationInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.B -> {
                stopGame()
                true
            }
            else -> true
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

            _uiState.update {
                it.copy(
                    screen = Screen.EMULATION,
                    emulationStatus = "Starting ${game.displayName}...",
                )
            }

            launchEmulationLoop()
        }
    }

    private fun launchEmulationLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastPublishedFrame = -1L

            while (isActive && _uiState.value.screen == Screen.EMULATION) {
                if (!PocketEngine.runFrame()) {
                    _uiState.update {
                        it.copy(emulationStatus = "Emulation stopped unexpectedly.")
                    }
                    break
                }

                val frameNumber = PocketEngine.frameCount()
                if (frameNumber != lastPublishedFrame) {
                    val width = PocketEngine.frameWidth()
                    val height = PocketEngine.frameHeight()
                    val pixels = PocketEngine.copyFrameRgba()

                    if (width > 0 && height > 0 && pixels.size == width * height) {
                        _uiState.update {
                            it.copy(
                                emulationStatus = "mGBA  ·  ${width}×${height}  ·  frame $frameNumber",
                                emulationFrame = pixels,
                                emulationWidth = width,
                                emulationHeight = height,
                            )
                        }
                        lastPublishedFrame = frameNumber
                    }
                }

                delay(16)
            }
        }
    }

    private fun stopGame() {
        PocketEngine.unloadGame()
        romRuntimeStager.clear()
        _uiState.update {
            it.copy(
                screen = Screen.PLATFORM,
                emulationStatus = "",
                emulationFrame = IntArray(0),
                emulationWidth = 0,
                emulationHeight = 0,
            )
        }
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
            val games = romScanner.scan(platform, folderUri)

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
