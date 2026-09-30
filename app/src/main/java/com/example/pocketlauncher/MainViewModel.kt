package com.example.pocketlauncher

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketlauncher.input.PocketButton
import com.example.pocketlauncher.input.PocketInputMapper
import com.example.pocketlauncher.ui.input.ButtonEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─────────────────────────────────────────────────────────────────────────────
// Application-level UI state
// ─────────────────────────────────────────────────────────────────────────────

enum class Screen { HOME, INPUT_TEST }

data class PocketUiState(
    val screen          : Screen             = Screen.HOME,
    val menuIndex       : Int                = 0,
    val engineReady     : Boolean            = false,
    val inputEvents     : List<ButtonEvent>  = emptyList(),
    val currentlyHeld   : Set<PocketButton>  = emptySet(),
)

private val MENU_SIZE = 5   // matches HomeScreen menu items

/**
 * MainViewModel
 *
 * Single source of truth for application UI state in Phase 0.
 * Handles:
 *   - Screen routing
 *   - D-pad navigation (UP/DOWN select, A confirms, B goes back)
 *   - Input event capture for the InputTestScreen
 *   - Engine readiness state (updated by JNI in later phases)
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PocketUiState())
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // Called from MainActivity.onKeyDown / onKeyUp
    // Returns true if the event was consumed by PocketLauncher.
    // -------------------------------------------------------------------------
    fun onKeyEvent(event: KeyEvent, pressed: Boolean): Boolean {
        val button = PocketInputMapper.map(event) ?: return false

        // Always update InputTest event log if that screen is visible
        if (_uiState.value.screen == Screen.INPUT_TEST) {
            recordInputEvent(button, pressed)
        }

        // Only act on key-down for navigation to avoid double-firing
        if (!pressed) return true

        return when (_uiState.value.screen) {
            Screen.HOME       -> handleHomeInput(button)
            Screen.INPUT_TEST -> handleInputTestInput(button)
        }
    }

    // ── Home screen navigation ────────────────────────────────────────────────

    private fun handleHomeInput(button: PocketButton): Boolean {
        return when (button) {
            PocketButton.UP -> {
                _uiState.update { it.copy(menuIndex = (it.menuIndex - 1 + MENU_SIZE) % MENU_SIZE) }
                true
            }
            PocketButton.DOWN -> {
                _uiState.update { it.copy(menuIndex = (it.menuIndex + 1) % MENU_SIZE) }
                true
            }
            PocketButton.A -> {
                // Index 4 = Settings → go to Input Test
                if (_uiState.value.menuIndex == 4) {
                    _uiState.update { it.copy(screen = Screen.INPUT_TEST) }
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

    // ── Input event log (for InputTestScreen) ─────────────────────────────────

    private fun recordInputEvent(button: PocketButton, pressed: Boolean) {
        _uiState.update { state ->
            val newHeld = if (pressed) state.currentlyHeld + button
                          else         state.currentlyHeld - button

            val newEvents = (state.inputEvents + ButtonEvent(button, pressed))
                .takeLast(100)   // cap log length

            state.copy(currentlyHeld = newHeld, inputEvents = newEvents)
        }
    }

    // -------------------------------------------------------------------------
    // Called from the JNI bridge (later phases) when the native engine is ready
    // -------------------------------------------------------------------------
    fun onEngineReady() {
        _uiState.update { it.copy(engineReady = true) }
    }
}
