package com.pocketlauncher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pocketlauncher.ui.screens.AppsScreen
import com.pocketlauncher.ui.screens.GamesScreen
import com.pocketlauncher.ui.screens.HomeScreen
import com.pocketlauncher.ui.screens.RetroSystemsScreen
import com.pocketlauncher.ui.screens.SystemRomListScreen
import com.pocketlauncher.ui.screens.SettingsScreen
import com.pocketlauncher.ui.screens.AboutScreen
import com.pocketlauncher.viewmodel.AppsViewModel
import com.pocketlauncher.viewmodel.HomeViewModel
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel

@Composable
fun PocketLauncherNavGraph(
    homeViewModel: HomeViewModel,
    appsViewModel: AppsViewModel,
    romViewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                homeViewModel = homeViewModel,
                appsViewModel = appsViewModel,
                romViewModel = romViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToGames = { navController.navigate("games") },
                onNavigateToApps = { navController.navigate("apps") },
                onNavigateToSystem = { systemName -> navController.navigate("system_roms/$systemName") },
                onNavigateToRetroSystems = { navController.navigate("retro_systems") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToAbout = { navController.navigate("about") }
            )
        }
        composable("games") {
            GamesScreen(
                viewModel = appsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("apps") {
            AppsScreen(
                viewModel = appsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("retro_systems") {
            RetroSystemsScreen(
                viewModel = romViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToSystem = { systemName -> navController.navigate("system_roms/$systemName") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("system_roms/{systemName}") { backStackEntry ->
            val systemName = backStackEntry.arguments?.getString("systemName") ?: ""
            SystemRomListScreen(
                systemName = systemName,
                homeViewModel = homeViewModel,
                romViewModel = romViewModel,
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                romViewModel = romViewModel,
                onNavigateToAbout = { navController.navigate("about") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
