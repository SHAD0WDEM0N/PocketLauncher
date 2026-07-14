package com.pocketlauncher

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.pocketlauncher.data.RomScanner
import com.pocketlauncher.data.local.AppDatabase
import com.pocketlauncher.navigation.PocketLauncherNavGraph
import com.pocketlauncher.repository.AppsRepository
import com.pocketlauncher.repository.RecentGamesRepository
import com.pocketlauncher.repository.RomRepository
import com.pocketlauncher.repository.SettingsRepository
import com.pocketlauncher.ui.theme.PocketLauncherTheme
import com.pocketlauncher.viewmodel.AppsViewModel
import com.pocketlauncher.viewmodel.HomeViewModel
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel
import com.pocketlauncher.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection
        val database = AppDatabase.getDatabase(this)
        val recentGamesRepository = RecentGamesRepository(database.recentGameDao())
        val appsRepository = AppsRepository(this)
        val romRepository = RomRepository(database.romDao(), database.romFolderDao())
        val romScanner = RomScanner(this)
        val settingsRepository = SettingsRepository(this)
        
        val viewModelFactory = ViewModelFactory(
            appsRepository, 
            recentGamesRepository,
            romRepository,
            romScanner,
            settingsRepository
        )
        val homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        val appsViewModel = ViewModelProvider(this, viewModelFactory)[AppsViewModel::class.java]
        val romViewModel = ViewModelProvider(this, viewModelFactory)[RomViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            PocketLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PocketLauncherNavGraph(
                        homeViewModel = homeViewModel,
                        appsViewModel = appsViewModel,
                        romViewModel = romViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
