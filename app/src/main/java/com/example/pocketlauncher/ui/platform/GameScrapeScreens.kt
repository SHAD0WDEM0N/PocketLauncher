package com.example.pocketlauncher.ui.platform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.scraper.ScrapeCandidate
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.ConsoleMenuScreen
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun GameOptionsScreen(
    game: GameEntry?,
    selectedIndex: Int,
    status: String,
) {
    ConsoleMenuScreen(
        title = game?.displayName?.uppercase() ?: "GAME OPTIONS",
        subtitle = when {
            status.isNotBlank() -> status
            game?.scrapeProvider != null -> "Matched via ${game.scrapeProvider}"
            else -> "No scraper match selected"
        },
        items = listOf(
            "Find / Change Match",
            "Rescrape This Game",
            "Clear Scraped Data",
            "Change ROM Folder",
        ),
        selectedIndex = selectedIndex,
        footer = "A  SELECT     B  BACK",
    )
}

@Composable
fun ScrapeMatchScreen(
    game: GameEntry?,
    candidates: List<ScrapeCandidate>,
    selectedIndex: Int,
    status: String,
    running: Boolean,
) {
    val metrics = pocketLayoutMetrics()
    val listState = rememberLazyListState()
    val safeIndex = selectedIndex.coerceIn(0, (candidates.size - 1).coerceAtLeast(0))

    LaunchedEffect(safeIndex, candidates.size) {
        if (candidates.isNotEmpty()) listState.animateScrollToItem(safeIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Text(
            text = "CHOOSE GAME MATCH",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = game?.fileName ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(20.dp))

        when {
            running -> {
                Text(
                    text = "SEARCHING THEGAMESDB...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PocketAmber,
                )
            }

            candidates.isEmpty() -> {
                Text(
                    text = status.ifBlank { "NO MATCHES FOUND" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            else -> {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(candidates, key = { _, item -> item.id }) { index, candidate ->
                        MatchCandidateCard(
                            candidate = candidate,
                            selected = index == safeIndex,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                val selected = candidates[safeIndex]
                Text(
                    text = selected.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    MatchMeta("REGION", selected.region ?: "—")
                    MatchMeta("RELEASE", selected.releaseDate ?: "—")
                    MatchMeta("RATING", selected.rating ?: "—")
                    MatchMeta("SOURCE", selected.provider)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = if (candidates.isNotEmpty()) {
                "◀  ▶  CHOOSE MATCH    A  APPLY    B  BACK"
            } else {
                "B  BACK"
            },
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MatchCandidateCard(
    candidate: ScrapeCandidate,
    selected: Boolean,
) {
    val bg = if (selected) Color(0xFFEDEAE3) else Color(0xFF181818)
    val border = if (selected) Color.White else Color(0xFF303030)
    val text = if (selected) Color(0xFF171717) else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .width(170.dp)
            .height(190.dp)
            .background(bg)
            .border(1.dp, border)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 128.dp, height = 126.dp)
                .background(if (selected) Color(0xFFD9D6CF) else Color(0xFF232323)),
            contentAlignment = Alignment.Center,
        ) {
            if (candidate.artworkUrl != null) {
                CandidateArtwork(
                    url = candidate.artworkUrl,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = "NO ART",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.DarkGray else PocketWhiteMuted,
                )
            }
        }

        Spacer(Modifier.height(7.dp))

        if (!candidate.region.isNullOrBlank()) {
            Text(
                text = candidate.region.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color(0xFF555555) else PocketWhiteMuted,
            )
            Spacer(Modifier.height(3.dp))
        }

        Text(
            text = candidate.title,
            style = MaterialTheme.typography.labelMedium,
            color = text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MatchMeta(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Text(
            text = value.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun CandidateArtwork(
    url: String,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(url) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
    }

    bitmap?.let { image ->
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}
