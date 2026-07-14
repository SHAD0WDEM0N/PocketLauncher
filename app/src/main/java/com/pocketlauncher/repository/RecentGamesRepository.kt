package com.pocketlauncher.repository

import com.pocketlauncher.data.local.RecentGameDao
import com.pocketlauncher.data.local.RecentGameEntity
import kotlinx.coroutines.flow.Flow

class RecentGamesRepository(private val dao: RecentGameDao) {
    
    fun getRecentGames(): Flow<List<RecentGameEntity>> {
        return dao.getRecentGames()
    }
    
    suspend fun addRecentGame(game: RecentGameEntity) {
        dao.insertRecentGame(game)
    }
}
