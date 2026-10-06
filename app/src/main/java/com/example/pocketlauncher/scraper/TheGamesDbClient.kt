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
                val queries = scraperQueries(scrapeTitle)
                var json: JSONObject? = null
                var games: JSONArray? = null
                var match: JSONObject? = null

                for (query in queries) {
                    val encodedName = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
                    val requestUrl = buildString {
                        append("https://api.thegamesdb.net/v1.1/Games/ByGameName")
                        append("?apikey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name()))
                        append("&name=").append(encodedName)
                        append("&filter%5Bplatform%5D=").append(platformId)
                        append("&fields=rating,platform")
                        append("&include=boxart,platform")
                    }

                    val candidateJson = JSONObject(get(requestUrl))
                    val candidateGames = candidateJson.getJSONObject("data").optJSONArray("games") ?: JSONArray()
                    if (candidateGames.length() == 0) continue

                    val candidateMatch = chooseBestMatch(scrapeTitle, candidateGames) ?: continue
                    json = candidateJson
                    games = candidateGames
                    match = candidateMatch
                    break
                }

                val matchedJson = json ?: error("No match found for ${game.displayName}")
                val matchedGame = match ?: error("No suitable match found for ${game.displayName}")

                val gameId = matchedGame.getInt("id").toString()
                val include = matchedJson.optJSONObject("include")
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
                    title = matchedGame.optString("game_title").takeIf { it.isNotBlank() },
                    artworkUrl = artworkUrl,
                    developer = null,
                    publisher = null,
                    releaseDate = matchedGame.optString("release_date").takeIf { it.isNotBlank() },
                    genre = null,
                    rating = matchedGame.optString("rating").takeIf { it.isNotBlank() },
                    provider = "TheGamesDB",
                )
            }
        }

    private fun scraperTitle(value: String): String {
        val stripped = value
            .replace(Regex("""\([^)]*\)"""), " ")
            .replace(Regex("""\[[^]]*]"""), " ")
            .replace(Regex("""\{[^}]*}"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val articlePattern = Regex("""^(.+),\s*(The|A|An)\s*[-:]\s*(.+)$""", RegexOption.IGNORE_CASE)
        val match = articlePattern.matchEntire(stripped)
        return if (match != null) {
            val base = match.groupValues[1].trim()
            val article = match.groupValues[2].trim()
            val rest = match.groupValues[3].trim()
            "$article $base: $rest"
        } else {
            stripped
        }
    }

    private fun scraperQueries(title: String): List<String> {
        val slashVariant = title.replace(" & ", " / ")
        val andVariant = title.replace(" & ", " and ")
        val punctuationFree = title
            .replace(Regex("""[:/\\-]+"""), " ")
            .replace(" & ", " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val subtitle = title.substringAfter(':', "").trim()
        val beforeAmpersand = subtitle.substringBefore(" & ").trim()
        val beforeSlash = subtitle.substringBefore(" / ").trim()
        val franchisePrefix = title.substringBefore(':').trim()

        return linkedSetOf(
            title,
            slashVariant,
            andVariant,
            punctuationFree,
            if (subtitle.isNotBlank()) subtitle else title,
            if (beforeAmpersand.isNotBlank()) beforeAmpersand else title,
            if (beforeSlash.isNotBlank()) beforeSlash else title,
            franchisePrefix,
        )
            .map { it.replace(Regex("""\s+"""), " ").trim() }
            .filter { it.length >= 4 }
    }
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

        return if (bestScore >= 8.0) best else null
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
