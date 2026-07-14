package com.pocketlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketlauncher.data.RomScanner
import com.pocketlauncher.repository.AppsRepository
import com.pocketlauncher.repository.RecentGamesRepository
import com.pocketlauncher.repository.RomRepository
import com.pocketlauncher.repository.SettingsRepository

class ViewModelFactory(
    private val appsRepository: AppsRepository,
    private val recentGamesRepository: RecentGamesRepository,
    private val romRepository: RomRepository,
    private val romScanner: RomScanner,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(recentGamesRepository) as T
        }
        if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppsViewModel(appsRepository, recentGamesRepository) as T
        }
        if (modelClass.isAssignableFrom(RomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RomViewModel(romRepository, romScanner) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, appsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
