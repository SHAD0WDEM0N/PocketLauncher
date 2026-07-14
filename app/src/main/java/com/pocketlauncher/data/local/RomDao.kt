package com.pocketlauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RomDao {
    @Query("SELECT * FROM roms ORDER BY title ASC")
    fun getAllRoms(): Flow<List<RomEntity>>

    @Query("SELECT * FROM roms WHERE systemName = :systemName ORDER BY title ASC")
    fun getRomsBySystem(systemName: String): Flow<List<RomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoms(roms: List<RomEntity>)

    @Query("DELETE FROM roms")
    suspend fun deleteAllRoms()

    @Query("DELETE FROM roms WHERE folderUri = :folderUri")
    suspend fun deleteRomsInFolder(folderUri: String)
}
