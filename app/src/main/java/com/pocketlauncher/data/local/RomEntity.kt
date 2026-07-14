package com.pocketlauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roms")
data class RomEntity(
    @PrimaryKey val filePath: String,
    val title: String,
    val systemName: String,
    val folderUri: String,
    val artworkPath: String? = null,
    val lastPlayed: Long = 0
)
