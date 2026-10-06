package com.example.pocketlauncher.scraper

import android.content.Context
import com.example.pocketlauncher.library.GameEntry
import org.json.JSONObject
import java.security.MessageDigest

data class ScrapedGameData(
    val title: String?,
    val artworkUrl: String?,
    val developer: String?,
    val publisher: String?,
    val releaseDate: String?,
    val genre: String?,
    val rating: String?,
    val provider: String,
)

class ScrapeCache(context: Context) {
    private val prefs = context.getSharedPreferences("scrape_cache", Context.MODE_PRIVATE)

    fun get(game: GameEntry): ScrapedGameData? {
        val raw = prefs.getString(key(game), null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            ScrapedGameData(
                title = json.optString("title").takeIf { it.isNotBlank() },
                artworkUrl = json.optString("artworkUrl").takeIf { it.isNotBlank() },
                developer = json.optString("developer").takeIf { it.isNotBlank() },
                publisher = json.optString("publisher").takeIf { it.isNotBlank() },
                releaseDate = json.optString("releaseDate").takeIf { it.isNotBlank() },
                genre = json.optString("genre").takeIf { it.isNotBlank() },
                rating = json.optString("rating").takeIf { it.isNotBlank() },
                provider = json.optString("provider", "Unknown"),
            )
        }.getOrNull()
    }

    fun put(game: GameEntry, data: ScrapedGameData) {
        val json = JSONObject()
            .put("title", data.title ?: "")
            .put("artworkUrl", data.artworkUrl ?: "")
            .put("developer", data.developer ?: "")
            .put("publisher", data.publisher ?: "")
            .put("releaseDate", data.releaseDate ?: "")
            .put("genre", data.genre ?: "")
            .put("rating", data.rating ?: "")
            .put("provider", data.provider)
        prefs.edit().putString(key(game), json.toString()).apply()
    }

    private fun key(game: GameEntry): String {
        val input = "${game.platform.name}|${game.fileName.lowercase()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
