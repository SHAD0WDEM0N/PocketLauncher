package com.example.pocketlauncher.engine

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RomRuntimeStager(
    private val context: Context,
) {
    suspend fun stage(uriString: String, fileName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val source = Uri.parse(uriString)
            val safeName = fileName.replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
            val dir = File(context.filesDir, "runtime-rom").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }

            val target = File(dir, safeName)
            context.contentResolver.openInputStream(source).use { input ->
                requireNotNull(input) { "Could not open selected ROM" }
                target.outputStream().use { output -> input.copyTo(output) }
            }

            require(target.exists() && target.length() > 0L) { "ROM copy failed" }
            target
        }
    }

    fun clear() {
        File(context.filesDir, "runtime-rom").deleteRecursively()
    }
}
