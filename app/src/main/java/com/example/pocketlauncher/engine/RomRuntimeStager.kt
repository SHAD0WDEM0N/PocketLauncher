package com.example.pocketlauncher.engine

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

class RomRuntimeStager(
    private val context: Context,
) {
    suspend fun stage(
        uriString: String,
        fileName: String,
        validExtensions: Set<String>,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val source = Uri.parse(uriString)
            val dir = File(context.filesDir, "runtime-rom").apply { mkdirs() }
            dir.listFiles()?.forEach { it.deleteRecursively() }

            val extension = fileName.substringAfterLast('.', "").lowercase()
            if (extension == "zip") {
                extractArchive(source, dir, validExtensions)
            } else {
                val safeName = fileName.replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
                val target = File(dir, safeName)
                context.contentResolver.openInputStream(source).use { input ->
                    requireNotNull(input) { "Could not open selected ROM" }
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                require(target.exists() && target.length() > 0L) { "ROM copy failed" }
                target
            }
        }
    }

    private fun extractArchive(
        source: Uri,
        dir: File,
        validExtensions: Set<String>,
    ): File {
        context.contentResolver.openInputStream(source).use { rawInput ->
            requireNotNull(rawInput) { "Could not open ROM archive" }
            ZipInputStream(rawInput.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val name = entry.name.substringAfterLast('/')
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (ext in validExtensions) {
                            val safeName = name.replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
                            val target = File(dir, safeName)
                            target.outputStream().use { output -> zip.copyTo(output) }
                            require(target.exists() && target.length() > 0L) { "ROM extraction failed" }
                            return target
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        error("Archive does not contain a supported ROM: ${validExtensions.joinToString()}")
    }

    fun clear() {
        File(context.filesDir, "runtime-rom").deleteRecursively()
    }
}
