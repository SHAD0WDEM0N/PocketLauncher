package com.example.pocketlauncher.scraper

import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer

class TheGamesDbClient {
    suspend fun scrape(game: GameEntry, apiKey: String): Result<ScrapedGameData> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(apiKey.isNotBlank()) { "TheGamesDB API key is missing" }
                val platformId = platformId(game.platform)
                    ?: error("Platform is not supported by TheGamesDB scraper yet")

                val scrapeTitle = scraperTitle(game.displayName)
                val encodedName = URLEncoder.encode(scrapeTitle, StandardCharsets.UTF_8.name())
                val requestUrl = buildString {
                    append("https://api.thegamesdb.net/v1.1/Games/ByGameName")
                    append("?apikey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name()))
                    append("&name=").append(encodedName)
                    append("&filter%5Bplatform%5D=").append(platformId)
                    append("&fields=rating,platform")
                    append("&include=boxart,platform")
                }

                val json = JSONObject(get(requestUrl))
                val games = json.getJSONObject("data").optJSONArray("games") ?: JSONArray()
                if (games.length() == 0) error("No match found for ${game.displayName}")

                val match = chooseBestMatch(scrapeTitle, games)
                    ?: error("No suitable match found for ${game.displayName}")

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

    private fun scraperTitle(value: String): String =
        value
            .replace(Regex("""\([^)]*\)"""), " ")
            .replace(Regex("""\[[^]]*]"""), " ")
            .replace(Regex("""\{[^}]*}"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    private fun chooseBestMatch(query: String, games: JSONArray): JSONObject? {
        val normalizedQuery = normalizeTitle(query)
        val queryTokens = normalizedQuery.split(' ').filter { it.isNotBlank() }.toSet()

        var best: JSONObject? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for (index in 0 until games.length()) {
            val candidate = games.optJSONObject(index) ?: continue
            val title = candidate.optString("game_title")
            if (title.isBlank()) continue

            val normalizedTitle = normalizeTitle(title)
            val candidateTokens = normalizedTitle.split(' ').filter { it.isNotBlank() }.toSet()

            val exactBonus = if (normalizedTitle == normalizedQuery) 100.0 else 0.0
            val containsBonus = when {
                normalizedTitle.startsWith(normalizedQuery) -> 12.0
                normalizedTitle.contains(normalizedQuery) -> 8.0
                else -> 0.0
            }
            val overlap = if (queryTokens.isEmpty()) 0.0 else {
                queryTokens.intersect(candidateTokens).size.toDouble() / queryTokens.size.toDouble()
            }
            val extraPenalty = (candidateTokens - queryTokens).size * 1.5
            val variantPenalty = if (listOf("special", "edition", "rando", "hack", "redux").any { it in candidateTokens && it !in queryTokens }) 8.0 else 0.0
            val score = exactBonus + containsBonus + overlap * 20.0 - extraPenalty - variantPenalty

            if (score > bestScore) {
                bestScore = score
                best = candidate
            }
        }

        return best
    }

    private fun normalizeTitle(value: String): String {
        val ascii = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace("&", " and ")
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return ascii
            .replace("pokemon", "pokemon")
            .replace(" version", "")
            .trim()
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
