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
)
