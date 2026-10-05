package com.example.pocketlauncher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.pocketlauncher.theme.PocketLauncherTheme

/**
 * MainActivity — single-activity host for PocketLauncher.
 *
 * Responsibilities:
 *   1. Full-screen immersive mode.
 *   2. Route hardware controller KeyEvents through MainViewModel.
 *   3. Host Compose navigation.
 *   4. Launch Android's Storage Access Framework folder picker when requested.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) return@registerForActivityResult

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (e: SecurityException) {
                // Some document providers grant access for the current session but
                // do not support persisted permissions. Scanning can still proceed.
                Log.w("PocketLauncher", "Could not persist folder permission: ${e.message}")
            }

            viewModel.onFolderSelected(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUI()

        setContent {
            PocketLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.folderPickerRequested) {
                        if (uiState.folderPickerRequested) {
                            folderPickerLauncher.launch(null)
                            viewModel.onFolderPickerLaunched()
                        }
                    }

                    MainNavigation(
                        uiState = uiState,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Controller input — all raw KeyEvents flow here before Compose sees them
    // -------------------------------------------------------------------------

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (viewModel.onKeyEvent(event, pressed = true)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (viewModel.onKeyEvent(event, pressed = false)) return true
        return super.onKeyUp(keyCode, event)
    }

    // -------------------------------------------------------------------------
    // Immersive full-screen — critical for a launcher / console feel
    // -------------------------------------------------------------------------

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}
