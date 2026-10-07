package com.example.pocketlauncher.engine

import android.content.Context
import com.example.pocketlauncher.library.GameEntry
import java.io.File

class BatterySaveManager(
    private val context: Context,
) {
    fun saveFile(game: GameEntry): File {
        val baseName = game.fileName.substringBeforeLast('.', game.fileName)
            .replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
            .ifBlank { "game" }

        val dir = File(context.filesDir, "saves/${game.platform.name.lowercase()}")
            .apply { mkdirs() }

        return File(dir, "$baseName.srm")
    }
}
