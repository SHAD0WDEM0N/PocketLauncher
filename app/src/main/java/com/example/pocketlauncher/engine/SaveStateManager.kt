package com.example.pocketlauncher.engine

import android.content.Context
import com.example.pocketlauncher.library.GameEntry
import java.io.File

class SaveStateManager(
    private val context: Context,
) {
    fun slotFile(game: GameEntry, slot: Int = 0): File {
        val baseName = game.fileName.substringBeforeLast('.', game.fileName)
            .replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
            .ifBlank { "game" }

        val dir = File(context.filesDir, "states/${game.platform.name.lowercase()}")
            .apply { mkdirs() }

        return File(dir, "$baseName.slot$slot.state")
    }
}
