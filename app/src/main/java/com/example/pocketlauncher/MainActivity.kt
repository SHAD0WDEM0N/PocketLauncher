package com.example.pocketlauncher

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color
import com.example.pocketlauncher.theme.PocketBackground\nimport com.example.pocketlauncher.theme.PocketLauncherTheme

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
                Log.w("PocketLauncher", "Could not persist folder permission: ${e.message}")
            }

            viewModel.onFolderSelected(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        allowDisplayCutout()
        hideSystemUI()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            PocketLauncherTheme(
                mode = uiState.themeMode,
                accent = uiState.themeAccent,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PocketBackground(
                        style = uiState.backgroundStyle,
                        modifier = Modifier.fillMaxSize(),
                    )

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                    ) {
                        LaunchedEffect(uiState.folderPickerRequested) {
                            if (uiState.folderPickerRequested) {
                                folderPickerLauncher.launch(null)
                                viewModel.onFolderPickerLaunched()
                            }
                        }

                        LaunchedEffect(uiState.systemSettingsRequested) {
                            if (uiState.systemSettingsRequested) {
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                                viewModel.onSystemSettingsLaunched()
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
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (viewModel.onKeyEvent(event, pressed = true)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (viewModel.onKeyEvent(event, pressed = false)) return true
        return super.onKeyUp(keyCode, event)
    }

    private fun allowDisplayCutout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    } else {
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
            }
        }
    }

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
