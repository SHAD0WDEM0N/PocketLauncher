package com.pocketlauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentGameDao {
    @Query("SELECT * FROM recent_games ORDER BY launchTime DESC LIMIT 50")
    fun getRecentGames(): Flow<List<RecentGameEntity>>

    @Query("SELECT * FROM recent_games WHERE sourceType = :type ORDER BY launchTime DESC LIMIT :limit")
    fun getRecentByType(type: String, limit: Int): Flow<List<RecentGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentGame(game: RecentGameEntity)
}
