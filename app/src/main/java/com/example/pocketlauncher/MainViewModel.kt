package com.example.pocketlauncher

import android.app.Application
import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketlauncher.engine.PocketEngine
import com.example.pocketlauncher.input.PocketButton
import com.example.pocketlauncher.input.PocketInputMapper
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.library.RomFolderStore
import com.example.pocketlauncher.library.RomScanner
import com.example.pocketlauncher.ui.input.ButtonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Application-level UI state
// ─────────────────────────────────────────────────────────────────────────────

enum class Screen { HOME, PLATFORM, INPUT_TEST }

data class PocketUiState(
    val screen                  : Screen             = Screen.HOME,
    val menuIndex               : Int                = 0,
    val engineReady             : Boolean            = false,
    val inputEvents             : List<ButtonEvent>  = emptyList(),
    val currentlyHeld           : Set<PocketButton>  = emptySet(),

    // Library / platform state
    val selectedPlatform        : Platform?           = null,
    val games                   : List<GameEntry>      = emptyList(),
    val gameIndex               : Int                  = 0,
    val currentFolderUri        : String?              = null,
    val currentFolderLabel      : String?              = null,
    val isScanning              : Boolean              = false,

    // One-shot request observed by MainActivity to open Android's folder picker.
    val folderPickerRequested   : Boolean              = false,
)

private const val MENU_SIZE = 5

/**
 * MainViewModel
 *
 * Single source of truth for PocketLauncher UI state.
 *
 * Phase 1 adds:
 *   - GB / GBC / GBA platform routing
 *   - Persistent ROM folder mapping
 *   - Folder scanning
 *   - Controller navigation inside a platform library
 */
class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val folderStore = RomFolderStore(application)
    private val romScanner = RomScanner(application)

    private val _uiState = MutableStateFlow(
        PocketUiState(
            engineReady = PocketEngine.getStatus()?.equals("READY", ignoreCase = true) == true,
        )
    )
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // Controller input
    // -------------------------------------------------------------------------

    fun onKeyEvent(event: KeyEvent, pressed: Boolean): Boolean {
        val button = PocketInputMapper.map(event) ?: return false

        if (_uiState.value.screen == Screen.INPUT_TEST) {
            recordInputEvent(button, pressed)
        }

        // Navigation acts on key-down only to avoid double-firing.
        if (!pressed) return true

        return when (_uiState.value.screen) {
            Screen.HOME       -> handleHomeInput(button)
            Screen.PLATFORM   -> handlePlatformInput(button)
            Screen.INPUT_TEST -> handleInputTestInput(button)
        }
    }

    // ── Home screen navigation ────────────────────────────────────────────────

    private fun handleHomeInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.UP -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex - 1 + MENU_SIZE) % MENU_SIZE)
                }
                true
            }

            PocketButton.DOWN -> {
                _uiState.update {
                    it.copy(menuIndex = (it.menuIndex + 1) % MENU_SIZE)
                }
                true
            }

            PocketButton.A -> {
                when (_uiState.value.menuIndex) {
                    1 -> openPlatform(Platform.GB)
                    2 -> openPlatform(Platform.GBC)
                    3 -> openPlatform(Platform.GBA)
                    4 -> _uiState.update { it.copy(screen = Screen.INPUT_TEST) }
                    // Recently Played stays a placeholder for now.
                }
                true
            }

            else -> false
        }
    }

    // ── Platform screen navigation ────────────────────────────────────────────

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
                }
                // Game launching is intentionally reserved for the libretro milestone.
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

    // ── Input test navigation ─────────────────────────────────────────────────

    private fun handleInputTestInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.B -> {
                _uiState.update { it.copy(screen = Screen.HOME) }
                true
            }

            else -> false
        }
    }

    // -------------------------------------------------------------------------
    // Platform / ROM folder workflow
    // -------------------------------------------------------------------------

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

    /**
     * Called immediately after MainActivity launches the system folder picker.
     * This clears the one-shot request so returning from the picker cannot launch
     * it again during recomposition.
     */
    fun onFolderPickerLaunched() {
        _uiState.update { it.copy(folderPickerRequested = false) }
    }

    /**
     * Called by MainActivity after Android grants access to a selected folder.
     */
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

            // Ignore stale results if the user backed out or changed platform.
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

    // ── Input event log ───────────────────────────────────────────────────────

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
