package com.pocketlauncher.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RomFolderDao {
    @Query("SELECT * FROM rom_folder_assignments")
    fun getAllFolderAssignments(): Flow<List<RomFolderAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderAssignment(assignment: RomFolderAssignment)

    @Delete
    suspend fun deleteFolderAssignment(assignment: RomFolderAssignment)

    @Query("DELETE FROM rom_folder_assignments")
    suspend fun clearAllFolderAssignments()
}
