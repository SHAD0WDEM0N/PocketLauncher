package com.pocketlauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_games")
data class RecentGameEntity(
    @PrimaryKey val id: String, // package name or rom file path
    val title: String,
    val launchTime: Long,
    val sourceType: String, // "ANDROID_GAME", "ANDROID_APP", "ROM"
    val artworkPath: String? = null
)
