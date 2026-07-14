package com.pocketlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlauncher.data.local.RecentGameEntity
import com.pocketlauncher.data.local.RomEntity
import com.pocketlauncher.repository.RecentGamesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recentGamesRepository: RecentGamesRepository
) : ViewModel() {

    private val allRecent = recentGamesRepository.getRecentGames()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentRoms: StateFlow<List<RecentGameEntity>> = allRecent.map { list ->
        list.filter { it.sourceType == "RETRO_ROM" }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentGames: StateFlow<List<RecentGameEntity>> = allRecent.map { list ->
        list.filter { it.sourceType == "ANDROID_GAME" }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentApps: StateFlow<List<RecentGameEntity>> = allRecent.map { list ->
        list.filter { it.sourceType == "ANDROID_APP" }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onGameLaunched(game: RecentGameEntity) {
        viewModelScope.launch {
            recentGamesRepository.addRecentGame(
                game.copy(launchTime = System.currentTimeMillis())
            )
        }
    }

    fun onRomLaunched(rom: RomEntity) {
        viewModelScope.launch {
            recentGamesRepository.addRecentGame(
                RecentGameEntity(
                    id = rom.filePath,
                    title = rom.title,
                    launchTime = System.currentTimeMillis(),
                    sourceType = "RETRO_ROM"
                )
            )
        }
    }
}
