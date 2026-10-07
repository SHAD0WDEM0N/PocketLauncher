package com.example.pocketlauncher.library

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Persists the Storage Access Framework tree URI selected for each platform.
 *
 * Persisted URI permissions are owned by Android; this store only remembers
 * which granted tree belongs to which PocketLauncher platform.
 */
class RomFolderStore(
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFolderUri(platform: Platform): String? =
        prefs.getString(key(platform), null)

    fun setFolderUri(platform: Platform, uri: Uri) {
        prefs.edit().putString(key(platform), uri.toString()).apply()
    }

    fun removeFolder(platform: Platform) {
        prefs.edit().remove(key(platform)).apply()
    }

    fun getFolderLabel(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            DocumentFile.fromTreeUri(context, Uri.parse(uriString))?.name
        }.getOrNull()
    }

    private fun key(platform: Platform) = "rom_folder_${platform.name.lowercase()}"

    private companion object {
        const val PREFS_NAME = "pocketlauncher_library"
    }
}
