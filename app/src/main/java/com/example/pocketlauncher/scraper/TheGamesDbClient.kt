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
    val region: String?,
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

            val ranked = found.values
                .map { candidate -> candidate to scoreTitle(scrapeTitle, candidate.title) }
                .sortedByDescending { it.second }

            val bestScore = ranked.firstOrNull()?.second ?: Double.NEGATIVE_INFINITY
            ranked
                .filter { (_, score) ->
                    score >= 8.0 && (bestScore < 20.0 || score >= bestScore - 10.0)
                }
                .map { it.first }
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
                        region = game.optString("region").takeIf { it.isNotBlank() }
                            ?: game.optString("region_id").takeIf { it.isNotBlank() },
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
        val stripped = stripBracketedSegments(value)
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString(" ")
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

    private fun stripBracketedSegments(value: String): String {
        val out = StringBuilder(value.length)
        var roundDepth = 0
        var squareDepth = 0
        var curlyDepth = 0

        for (char in value) {
            when (char) {
                '(' -> roundDepth += 1
                ')' -> if (roundDepth > 0) roundDepth -= 1
                '[' -> squareDepth += 1
                ']' -> if (squareDepth > 0) squareDepth -= 1
                '{' -> curlyDepth += 1
                '}' -> if (curlyDepth > 0) curlyDepth -= 1
                else -> {
                    if (roundDepth == 0 && squareDepth == 0 && curlyDepth == 0) {
                        out.append(char)
                    }
                }
            }

            if ((char == ')' || char == ']' || char == '}') &&
                roundDepth == 0 && squareDepth == 0 && curlyDepth == 0
            ) {
                out.append(' ')
            }
        }

        return out.toString()
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
        val queryTokens = normalizedQuery.split(' ').filter { it.isNotBlank() }
        val candidateTokens = normalizedCandidate.split(' ').filter { it.isNotBlank() }
        val querySet = queryTokens.toSet()
        val candidateSet = candidateTokens.toSet()

        val exactBonus = if (normalizedCandidate == normalizedQuery) 120.0 else 0.0
        val overlapCount = querySet.intersect(candidateSet).size
        val overlap = if (querySet.isEmpty()) 0.0 else overlapCount.toDouble() / querySet.size.toDouble()
        val reverseOverlap = if (candidateSet.isEmpty()) 0.0 else overlapCount.toDouble() / candidateSet.size.toDouble()

        val queryBigrams = queryTokens.zipWithNext { a, b -> "$a $b" }.toSet()
        val candidateBigrams = candidateTokens.zipWithNext { a, b -> "$a $b" }.toSet()
        val bigramOverlap = queryBigrams.intersect(candidateBigrams).size

        val importantTokens = querySet.filter { it.length >= 5 && it !in setOf("legend", "zelda", "game", "version") }.toSet()
        val importantMatches = importantTokens.intersect(candidateSet).size
        val importantMisses = (importantTokens - candidateSet).size

        val extraPenalty = (candidateSet - querySet).size * 1.5
        val importantPenalty = importantMisses * 5.0
        val importantBonus = importantMatches * 6.0
        val bigramBonus = bigramOverlap * 4.0

        return exactBonus +
            overlap * 35.0 +
            reverseOverlap * 20.0 +
            importantBonus +
            bigramBonus -
            extraPenalty -
            importantPenalty
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
        Platform.GB -> 4
        Platform.GBC -> 41
        Platform.GBA -> 5
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
