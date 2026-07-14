package com.pocketlauncher.repository

import com.pocketlauncher.data.local.RomDao
import com.pocketlauncher.data.local.RomEntity
import com.pocketlauncher.data.local.RomFolderAssignment
import com.pocketlauncher.data.local.RomFolderDao
import kotlinx.coroutines.flow.Flow

class RomRepository(
    private val romDao: RomDao,
    private val romFolderDao: RomFolderDao
) {
    
    fun getAllRoms(): Flow<List<RomEntity>> = romDao.getAllRoms()
    
    fun getRomsBySystem(system: String): Flow<List<RomEntity>> = romDao.getRomsBySystem(system)
    
    fun getAllFolderAssignments(): Flow<List<RomFolderAssignment>> = romFolderDao.getAllFolderAssignments()
    
    suspend fun saveRoms(roms: List<RomEntity>) {
        romDao.insertRoms(roms)
    }

    suspend fun addFolderAssignment(assignment: RomFolderAssignment) {
        romFolderDao.insertFolderAssignment(assignment)
    }

    suspend fun deleteFolderAssignment(assignment: RomFolderAssignment) {
        romFolderDao.deleteFolderAssignment(assignment)
        romDao.deleteRomsInFolder(assignment.folderUri)
    }
    
    suspend fun clearRoms() {
        romDao.deleteAllRoms()
    }
}
