package com.example.pocketlauncher

import androidx.compose.runtime.Composable
import com.example.pocketlauncher.ui.home.HomeScreen
import com.example.pocketlauncher.ui.input.InputTestScreen
import com.example.pocketlauncher.ui.platform.PlatformScreen

/**
 * Lightweight PocketLauncher screen router.
 *
 * We intentionally keep routing state in MainViewModel for now rather than
 * introducing a navigation dependency before the library flow is stable.
 */
@Composable
fun MainNavigation(
    uiState   : PocketUiState,
    viewModel : MainViewModel,
) {
    when (uiState.screen) {
        Screen.HOME -> HomeScreen(
            engineReady           = uiState.engineReady,
            selectedIndex         = uiState.menuIndex,
            onSelectIndexChanged  = { /* controller-driven */ },
            onNavigateToInputTest = { /* navigation handled in ViewModel */ },
        )

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

        Screen.INPUT_TEST -> InputTestScreen(
            events        = uiState.inputEvents,
            currentlyHeld = uiState.currentlyHeld,
        )
    }
}
