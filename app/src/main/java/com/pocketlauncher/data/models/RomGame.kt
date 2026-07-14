package com.pocketlauncher.data.models

data class RomGame(
    val title: String,
    val filePath: String,
    val system: String, // Or RomSystem
    val artworkPath: String?
)
