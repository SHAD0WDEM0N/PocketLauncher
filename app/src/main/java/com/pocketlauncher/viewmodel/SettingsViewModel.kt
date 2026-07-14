package com.pocketlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.repository.AppsRepository
import com.pocketlauncher.repository.SettingsRepository
import com.pocketlauncher.repository.EmulatorAssignment
import com.pocketlauncher.utils.RetroUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {

    val themeMode = settingsRepository.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")
    val autoScanStartup = settingsRepository.autoScanStartup.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showHiddenFiles = settingsRepository.showHiddenFiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val includeSubfolders = settingsRepository.includeSubfolders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val recentGamesCount = settingsRepository.recentGamesCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val showAppsHome = settingsRepository.showAppsHome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showRetroHome = settingsRepository.showRetroHome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _emulatorAssignments = MutableStateFlow<Map<String, EmulatorAssignment>>(emptyMap())
    val emulatorAssignments = _emulatorAssignments.asStateFlow()

    val allSystems = listOf(
        "Arcade", "Dreamcast", "Game Boy", "Game Boy Color", "Game Boy Advance",
        "GameCube", "NES", "Nintendo 64", "Nintendo DS", "Nintendo 3DS",
        "PS1", "PlayStation 2", "PSP", "Sega Genesis", "Sega Master System",
        "SNES", "Wii", "Xbox", "Xbox 360"
    )

    init {
        loadEmulatorAssignments()
    }

    private fun loadEmulatorAssignments() {
        viewModelScope.launch {
            _emulatorAssignments.value = settingsRepository.getAllEmulatorAssignments()
        }
    }

    fun setThemeMode(mode: String) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setAutoScanStartup(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoScanStartup(enabled) }
    fun setShowHiddenFiles(enabled: Boolean) = viewModelScope.launch { settingsRepository.setShowHiddenFiles(enabled) }
    fun setIncludeSubfolders(enabled: Boolean) = viewModelScope.launch { settingsRepository.setIncludeSubfolders(enabled) }
    fun setRecentGamesCount(count: Int) = viewModelScope.launch { settingsRepository.setRecentGamesCount(count) }
    fun setShowAppsHome(show: Boolean) = viewModelScope.launch { settingsRepository.setShowAppsHome(show) }
    fun setShowRetroHome(show: Boolean) = viewModelScope.launch { settingsRepository.setShowRetroHome(show) }

    fun saveEmulatorAssignment(systemName: String, packageName: String) = viewModelScope.launch {
        settingsRepository.saveEmulatorAssignment(systemName, packageName)
        loadEmulatorAssignments()
    }

    fun saveRetroArchCore(systemName: String, corePath: String?) = viewModelScope.launch {
        settingsRepository.saveRetroArchCore(systemName, corePath)
        loadEmulatorAssignments()
    }

    fun resetEmulatorAssignments() = viewModelScope.launch {
        settingsRepository.resetEmulatorAssignments()
        loadEmulatorAssignments()
    }

    fun autoConfigureEmulators() = viewModelScope.launch {
        val installed = appsRepository.getInstalledApps().associateBy { it.id }
        
        allSystems.forEach { system ->
            val standalonePkg = RetroUtils.recommendedStandalone[system]
            if (standalonePkg != null && installed.containsKey(standalonePkg)) {
                settingsRepository.saveEmulatorAssignment(system, standalonePkg)
                settingsRepository.saveRetroArchCore(system, null)
            } else {
                // Try RetroArch
                val retroArch = installed.values.find { it.id.contains("retroarch") }
                if (retroArch != null) {
                    val recommendedCore = RetroUtils.getRecommendedCores(system).firstOrNull()
                    settingsRepository.saveEmulatorAssignment(system, retroArch.id)
                    settingsRepository.saveRetroArchCore(system, recommendedCore?.fileName)
                }
            }
        }
        loadEmulatorAssignments()
    }

    suspend fun getInstalledApps(): List<AppItem> {
        return appsRepository.getInstalledApps()
    }
}
