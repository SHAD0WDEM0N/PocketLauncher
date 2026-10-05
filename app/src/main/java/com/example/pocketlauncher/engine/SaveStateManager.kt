package com.example.pocketlauncher.engine

import android.content.Context
import com.example.pocketlauncher.library.GameEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun slotSummary(game: GameEntry, slot: Int): String {
        val file = slotFile(game, slot)
        if (!file.exists() || file.length() == 0L) return "Empty"
        val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return formatter.format(Date(file.lastModified()))
    }
}
