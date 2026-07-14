package com.pocketlauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rom_folder_assignments")
data class RomFolderAssignment(
    @PrimaryKey val folderUri: String,
    val folderName: String,
    val systemName: String
)
