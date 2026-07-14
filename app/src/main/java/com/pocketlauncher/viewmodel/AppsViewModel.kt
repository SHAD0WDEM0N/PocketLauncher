package com.pocketlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlauncher.data.local.RecentGameEntity
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.repository.AppsRepository
import com.pocketlauncher.repository.RecentGamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppsViewModel(
    private val appsRepository: AppsRepository,
    private val recentGamesRepository: RecentGamesRepository
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()
    
    private val _games = MutableStateFlow<List<AppItem>>(emptyList())
    val games: StateFlow<List<AppItem>> = _games.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val installedApps = appsRepository.getInstalledApps()
            _games.value = installedApps.filter { it.isGame }
            _apps.value = installedApps.filter { !it.isGame }
            _isLoading.value = false
        }
    }
    
    fun onAppLaunched(appItem: AppItem) {
        viewModelScope.launch {
            recentGamesRepository.addRecentGame(
                RecentGameEntity(
                    id = appItem.id,
                    title = appItem.name,
                    launchTime = System.currentTimeMillis(),
                    sourceType = if (appItem.isGame) "ANDROID_GAME" else "ANDROID_APP"
                )
            )
        }
    }
}
