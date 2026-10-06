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

data class ScrapeCandidate(
    val id: Int,
    val title: String,
    val artworkUrl: String?,
    val releaseDate: String?,
    val rating: String?,
    val provider: String = "TheGamesDB",
) {
    fun toScrapedGameData(): ScrapedGameData = ScrapedGameData(
        title = title,
        artworkUrl = artworkUrl,
        developer = null,
        publisher = null,
        releaseDate = releaseDate,
        genre = null,
        rating = rating,
        provider = provider,
    )
}

class TheGamesDbClient {
    suspend fun scrape(game: GameEntry, apiKey: String): Result<ScrapedGameData> =
        searchCandidates(game, apiKey).mapCatching { candidates ->
            val scrapeTitle = scraperTitle(game.displayName)
            candidates.maxByOrNull { candidate ->
                scoreTitle(scrapeTitle, candidate.title)
            }?.toScrapedGameData()
                ?: error("No suitable match found for ${game.displayName}")
        }

    suspend fun searchCandidates(
        game: GameEntry,
        apiKey: String,
        limit: Int = 12,
    ): Result<List<ScrapeCandidate>> = withContext(Dispatchers.IO) {
        runCatching {
            require(apiKey.isNotBlank()) { "TheGamesDB API key is missing" }
            val platformId = platformId(game.platform)
                ?: error("Platform is not supported by TheGamesDB scraper yet")

            val scrapeTitle = scraperTitle(game.displayName)
            val found = linkedMapOf<Int, ScrapeCandidate>()

            for (query in scraperQueries(scrapeTitle)) {
                val response = requestCandidates(query, platformId, apiKey)
                for (candidate in response) {
                    found.putIfAbsent(candidate.id, candidate)
                    if (found.size >= limit) break
                }
                if (found.size >= limit) break
            }

            if (found.isEmpty()) error("No matches found for ${game.displayName}")

            found.values
                .sortedByDescending { scoreTitle(scrapeTitle, it.title) }
                .take(limit)
        }
    }

    private fun requestCandidates(
        query: String,
        platformId: Int,
        apiKey: String,
    ): List<ScrapeCandidate> {
        val encodedName = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val requestUrl = buildString {
            append("https://api.thegamesdb.net/v1.1/Games/ByGameName")
            append("?apikey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name()))
            append("&name=").append(encodedName)
            append("&filter%5Bplatform%5D=").append(platformId)
            append("&fields=rating,platform")
            append("&include=boxart,platform")
        }

        val json = JSONObject(get(requestUrl))
        val games = json.optJSONObject("data")?.optJSONArray("games") ?: JSONArray()
        val boxart = json.optJSONObject("include")?.optJSONObject("boxart")
        val baseUrl = boxart
            ?.optJSONObject("base_url")
            ?.optString("medium")
            ?.takeIf { it.isNotBlank() }
        val artworkData = boxart?.optJSONObject("data")

        return buildList {
            for (index in 0 until games.length()) {
                val game = games.optJSONObject(index) ?: continue
                val id = game.optInt("id", -1)
                val title = game.optString("game_title")
                if (id < 0 || title.isBlank()) continue

                add(
                    ScrapeCandidate(
                        id = id,
                        title = title,
                        artworkUrl = artworkUrlFor(id, baseUrl, artworkData),
                        releaseDate = game.optString("release_date").takeIf { it.isNotBlank() },
                        rating = game.optString("rating").takeIf { it.isNotBlank() },
                    )
                )
            }
        }
    }

    private fun artworkUrlFor(
        gameId: Int,
        baseUrl: String?,
        artworkData: JSONObject?,
    ): String? {
        if (baseUrl == null || artworkData == null) return null
        val images = artworkData.optJSONArray(gameId.toString()) ?: return null
        for (index in 0 until images.length()) {
            val image = images.optJSONObject(index) ?: continue
            if (image.optString("type") == "boxart" && image.optString("side") == "front") {
                val filename = image.optString("filename")
                if (filename.isNotBlank()) return baseUrl + filename
            }
        }
        return null
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
            subtitle,
            beforeAmpersand,
            beforeSlash,
            franchisePrefix,
        )
            .map { it.replace(Regex("""\s+"""), " ").trim() }
            .filter { it.length >= 4 }
    }

    private fun scoreTitle(query: String, candidate: String): Double {
        val normalizedQuery = normalizeTitle(query)
        val normalizedCandidate = normalizeTitle(candidate)
        val queryTokens = normalizedQuery.split(' ').filter { it.isNotBlank() }.toSet()
        val candidateTokens = normalizedCandidate.split(' ').filter { it.isNotBlank() }.toSet()
        val exactBonus = if (normalizedCandidate == normalizedQuery) 100.0 else 0.0
        val overlap = if (queryTokens.isEmpty()) 0.0 else {
            queryTokens.intersect(candidateTokens).size.toDouble() / queryTokens.size.toDouble()
        }
        val extraPenalty = (candidateTokens - queryTokens).size * 1.25
        return exactBonus + overlap * 30.0 - extraPenalty
    }

    private fun normalizeTitle(value: String): String =
        stripDiacritics(value)
            .lowercase()
            .replace("&", " and ")
            .replace("/", " and ")
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .replace(" version", "")
            .trim()

    private fun stripDiacritics(value: String): String {
        val decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
        return buildString(decomposed.length) {
            for (char in decomposed) {
                when (Character.getType(char)) {
                    Character.NON_SPACING_MARK.toInt(),
                    Character.COMBINING_SPACING_MARK.toInt(),
                    Character.ENCLOSING_MARK.toInt() -> Unit
                    else -> append(char)
                }
            }
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
