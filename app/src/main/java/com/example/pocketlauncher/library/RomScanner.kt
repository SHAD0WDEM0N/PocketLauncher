package com.example.pocketlauncher.library

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans one user-selected tree for ROMs belonging to a platform.
 *
 * Phase 1 intentionally scans the selected folder itself (not every nested
 * sub-folder). Recursive/multi-folder libraries can be added after the basic
 * workflow is proven on the handheld test devices.
 */
class RomScanner(
    private val context: Context,
) {
    suspend fun scan(platform: Platform, folderUri: String): List<GameEntry> =
        withContext(Dispatchers.IO) {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(folderUri))
                ?: return@withContext emptyList()

            runCatching {
                root.listFiles()
                    .asSequence()
                    .filter { it.isFile }
                    .filter { file ->
                        val extension = file.name
                            ?.substringAfterLast('.', missingDelimiterValue = "")
                            ?.lowercase()
                            .orEmpty()
                        extension in platform.extensions
                    }
                    .mapNotNull { file ->
                        val fileName = file.name ?: return@mapNotNull null
                        GameEntry(
                            displayName = fileName.substringBeforeLast('.'),
                            fileName = fileName,
                            uri = file.uri.toString(),
                            platform = platform,
                        )
                    }
                    .sortedBy { it.displayName.lowercase() }
                    .toList()
            }.getOrElse { emptyList() }
        }
}
