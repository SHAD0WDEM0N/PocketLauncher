package com.example.pocketlauncher.library

/**
 * A ROM discovered inside a user-selected platform folder.
 *
 * The content URI is retained instead of converting it to a filesystem path so
 * PocketLauncher remains compatible with Android's Storage Access Framework.
 */
data class GameEntry(
    val displayName: String,
    val fileName: String,
    val uri: String,
    val platform: Platform,
    val artworkUrl: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val releaseDate: String? = null,
    val genre: String? = null,
    val rating: String? = null,
    val scrapeProvider: String? = null,
    val lastPlayedEpochMs: Long = 0L,
    val playtimeSeconds: Long = 0L,
    val isFavourite: Boolean = false,
)
