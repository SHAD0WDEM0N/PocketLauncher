package com.example.pocketlauncher.library

import android.content.Context
import org.json.JSONObject
import java.security.MessageDigest

data class GameHistory(
    val lastPlayedEpochMs: Long = 0L,
    val playtimeSeconds: Long = 0L,
)

class PlayHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("play_history", Context.MODE_PRIVATE)

    fun get(game: GameEntry): GameHistory {
        val raw = prefs.getString(key(game), null) ?: return GameHistory()
        return runCatching {
            val json = JSONObject(raw)
            GameHistory(
                lastPlayedEpochMs = json.optLong("lastPlayedEpochMs", 0L),
                playtimeSeconds = json.optLong("playtimeSeconds", 0L),
            )
        }.getOrElse { GameHistory() }
    }

    fun recordSession(game: GameEntry, sessionSeconds: Long, endedAtEpochMs: Long = System.currentTimeMillis()): GameHistory {
        val current = get(game)
        val updated = GameHistory(
            lastPlayedEpochMs = endedAtEpochMs,
            playtimeSeconds = current.playtimeSeconds + sessionSeconds.coerceAtLeast(0L),
        )
        val json = JSONObject()
            .put("lastPlayedEpochMs", updated.lastPlayedEpochMs)
            .put("playtimeSeconds", updated.playtimeSeconds)
        prefs.edit().putString(key(game), json.toString()).apply()
        return updated
    }

    private fun key(game: GameEntry): String {
        val input = "${game.platform.name}|${game.fileName.lowercase()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
