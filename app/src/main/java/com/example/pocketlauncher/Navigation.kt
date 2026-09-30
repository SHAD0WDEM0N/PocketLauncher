package com.example.pocketlauncher

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.ui.home.HomeScreen
import com.example.pocketlauncher.ui.input.InputTestScreen

/**
 * MainNavigation — Phase 0 screen router.
 *
 * We deliberately avoid a navigation library in Phase 0 to keep
 * dependencies minimal. Simple when-expression routing is sufficient
 * until we have a real game library to navigate into.
 */
@Composable
fun MainNavigation(
    uiState   : PocketUiState,
    viewModel : MainViewModel,
) {
    when (uiState.screen) {
        Screen.HOME -> HomeScreen(
            engineReady          = uiState.engineReady,
            selectedIndex        = uiState.menuIndex,
            onSelectIndexChanged = { /* driven by controller, not touch */ },
            onNavigateToInputTest = { /* navigation handled in ViewModel */ },
        )

        Screen.INPUT_TEST -> InputTestScreen(
            events        = uiState.inputEvents,
            currentlyHeld = uiState.currentlyHeld,
        )
    }
}
