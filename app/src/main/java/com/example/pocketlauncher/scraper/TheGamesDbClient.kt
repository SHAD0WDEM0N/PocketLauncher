package com.example.pocketlauncher.scraper

import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TheGamesDbClient {
    suspend fun scrape(game: GameEntry, apiKey: String): Result<ScrapedGameData> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(apiKey.isNotBlank()) { "TheGamesDB API key is missing" }
                val platformId = platformId(game.platform)
                    ?: error("Platform is not supported by TheGamesDB scraper yet")

                val encodedName = URLEncoder.encode(game.displayName, StandardCharsets.UTF_8.name())
                val requestUrl = buildString {
                    append("https://api.thegamesdb.net/v1.1/Games/ByGameName")
                    append("?apikey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name()))
                    append("&name=").append(encodedName)
                    append("&filter%5Bplatform%5D=").append(platformId)
                    append("&fields=rating,platform")
                    append("&include=boxart,platform")
                }

                val json = JSONObject(get(requestUrl))
                val games = json.getJSONObject("data").getJSONArray("games")
                if (games.length() == 0) error("No match found for ${game.displayName}")

                val match = games.getJSONObject(0)
                val gameId = match.getInt("id").toString()
                val include = json.optJSONObject("include")
                val boxart = include?.optJSONObject("boxart")
                val baseUrl = boxart
                    ?.optJSONObject("base_url")
                    ?.optString("medium")
                    ?.takeIf { it.isNotBlank() }
                val images = boxart
                    ?.optJSONObject("data")
                    ?.optJSONArray(gameId)

                var artworkUrl: String? = null
                if (baseUrl != null && images != null) {
                    for (index in 0 until images.length()) {
                        val image = images.getJSONObject(index)
                        if (image.optString("type") == "boxart" && image.optString("side") == "front") {
                            val filename = image.optString("filename")
                            if (filename.isNotBlank()) {
                                artworkUrl = baseUrl + filename
                                break
                            }
                        }
                    }
                }

                ScrapedGameData(
                    title = match.optString("game_title").takeIf { it.isNotBlank() },
                    artworkUrl = artworkUrl,
                    developer = null,
                    publisher = null,
                    releaseDate = match.optString("release_date").takeIf { it.isNotBlank() },
                    genre = null,
                    rating = match.optString("rating").takeIf { it.isNotBlank() },
                    provider = "TheGamesDB",
                )
            }
        }

    private fun platformId(platform: Platform): Int? = when (platform) {
        Platform.GBA -> 5
        else -> null
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.useCaches = false

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) error("TheGamesDB returned HTTP $code")
            body
        } finally {
            connection.disconnect()
        }
    }
}
