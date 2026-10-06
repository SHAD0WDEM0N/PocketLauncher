package com.example.pocketlauncher.ui.platform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlatformScreen(
    platform: Platform,
    folderLabel: String?,
    games: List<GameEntry>,
    selectedIndex: Int,
    isScanning: Boolean,
) {
    val metrics = pocketLayoutMetrics()
    val listState = rememberLazyListState()
    val safeIndex = selectedIndex.coerceIn(0, (games.size - 1).coerceAtLeast(0))
    val selectedGame = games.getOrNull(safeIndex)

    LaunchedEffect(safeIndex, games.size) {
        if (games.isNotEmpty()) {
            listState.animateScrollToItem(safeIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = platform.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "L  R",
                style = MaterialTheme.typography.labelSmall,
                color = PocketWhiteMuted,
            )
        }

        Spacer(Modifier.height(20.dp))

        when {
            folderLabel == null -> {
                EmptyMessage(
                    title = "No ROM folder configured.",
                    subtitle = "Press A to select a folder for ${platform.displayName}.",
                )
            }

            isScanning -> {
                EmptyMessage(
                    title = "Scanning...",
                    subtitle = "Looking for ${platform.extensions.joinToString(" / ") { ".$it" }} files.",
                )
            }

            games.isEmpty() -> {
                EmptyMessage(
                    title = "No games found.",
                    subtitle = "Press Y to rescan or X to choose a different folder.",
                )
            }

            else -> {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(
                        items = games,
                        key = { _, game -> game.uri },
                    ) { index, game ->
                        CartridgeCard(
                            game = game,
                            selected = index == safeIndex,
                            platform = platform,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (selectedGame != null) {
                    Text(
                        text = selectedGame.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(18.dp))

                    GameMetadata(selectedGame)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = if (folderLabel == null) {
                "A  SELECT FOLDER     B  BACK"
            } else {
                "◀  ▶  BROWSE    A  PLAY    X  OPTIONS    Y  RESCAN    B  BACK"
            },
            style = MaterialTheme.typography.labelSmall.merge(
                TextStyle(fontSize = metrics.hintTextSize)
            ),
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RecentlyPlayedScreen(
    games: List<GameEntry>,
    selectedIndex: Int,
    isScanning: Boolean,
) {
    val metrics = pocketLayoutMetrics()
    val listState = rememberLazyListState()
    val safeIndex = selectedIndex.coerceIn(0, (games.size - 1).coerceAtLeast(0))
    val selectedGame = games.getOrNull(safeIndex)

    LaunchedEffect(safeIndex, games.size) {
        if (games.isNotEmpty()) listState.animateScrollToItem(safeIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))
        Text(
            text = "Recently Played",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))

        when {
            isScanning -> EmptyMessage("Loading...", "Reading recent play history.")
            games.isEmpty() -> EmptyMessage("No recent games yet.", "Launch and quit a game to add it here.")
            else -> {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(games, key = { _, game -> game.uri }) { index, game ->
                        CartridgeCard(
                            game = game,
                            selected = index == safeIndex,
                            platform = game.platform,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (selectedGame != null) {
                    Text(
                        text = selectedGame.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(18.dp))
                    GameMetadata(selectedGame)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            text = "◀  ▶  BROWSE    A  PLAY    X  OPTIONS    B  BACK",
            style = MaterialTheme.typography.labelSmall.merge(TextStyle(fontSize = metrics.hintTextSize)),
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}
@Composable
private fun CartridgeCard(
    game: GameEntry,
    selected: Boolean,
    platform: Platform,
) {
    val background = if (selected) Color(0xFFEDEAE3) else Color(0xFF181818)
    val border = if (selected) Color.White else Color(0xFF2A2A2A)

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(170.dp)
            .background(background)
            .border(1.dp, border),
        contentAlignment = Alignment.Center,
    ) {
        when (platform) {
            Platform.GBA -> GbaCartridgePlaceholder(
                game = game,
                selected = selected,
                modifier = Modifier.size(width = 126.dp, height = 102.dp),
            )
            Platform.GB, Platform.GBC -> GbCartridgePlaceholder(
                game = game,
                selected = selected,
                colorModel = platform == Platform.GBC,
                modifier = Modifier.size(width = 100.dp, height = 124.dp),
            )
        }
    }
}

@Composable
private fun GbaCartridgePlaceholder(
    game: GameEntry,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val shell = if (selected) Color(0xFF555753) else Color(0xFF363836)
    val ridge = if (selected) Color(0xFF777A75) else Color(0xFF4A4C49)
    val label = if (selected) Color(0xFF2F6940) else Color(0xFF255333)

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = shell,
                topLeft = Offset(w * 0.08f, h * 0.12f),
                size = Size(w * 0.84f, h * 0.76f),
                cornerRadius = CornerRadius(8f),
            )

            drawRect(
                color = ridge,
                topLeft = Offset(w * 0.16f, h * 0.08f),
                size = Size(w * 0.68f, h * 0.12f),
            )

            drawRoundRect(
                color = label,
                topLeft = Offset(w * 0.17f, h * 0.30f),
                size = Size(w * 0.66f, h * 0.40f),
                cornerRadius = CornerRadius(4f),
            )
        }

        if (game.artworkUrl != null) {
            RemoteArtwork(
                url = game.artworkUrl,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 5.dp)
                    .width(78.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )
        } else {
            Text(
                text = game.displayName.uppercase(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(72.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GbCartridgePlaceholder(
    game: GameEntry,
    selected: Boolean,
    colorModel: Boolean,
    modifier: Modifier = Modifier,
) {
    val shell = when {
        colorModel && selected -> Color(0xFF879BA5)
        colorModel -> Color(0xFF53626A)
        selected -> Color(0xFFB7B4AE)
        else -> Color(0xFF77746F)
    }

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = shell,
                topLeft = Offset(w * 0.08f, h * 0.04f),
                size = Size(w * 0.84f, h * 0.92f),
                cornerRadius = CornerRadius(7f),
            )

            drawRect(
                color = Color(0xFF31312F),
                topLeft = Offset(w * 0.17f, h * 0.29f),
                size = Size(w * 0.66f, h * 0.46f),
            )
        }

        Text(
            text = game.displayName.uppercase(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 12.dp)
                .width(66.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GameMetadata(game: GameEntry) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(46.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            MetadataRow("RATING", game.rating ?: "—")
            MetadataRow("GENRE", game.genre ?: "—")
            Spacer(Modifier.height(14.dp))
            MetadataRow("DEVELOPER", game.developer ?: "—")
            MetadataRow("PUBLISHER", game.publisher ?: "—")
            MetadataRow("RELEASE", game.releaseDate ?: "—")
        }

        Column {
            MetadataRow("LAST PLAYED", formatLastPlayed(game.lastPlayedEpochMs))
            MetadataRow("PLAYTIME", formatPlaytime(game.playtimeSeconds))
        }
    }
}

private fun formatLastPlayed(epochMs: Long): String {
    if (epochMs <= 0L) return "—"
    return SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault()).format(Date(epochMs))
}

private fun formatPlaytime(seconds: Long): String {
    if (seconds <= 0L) return "—"
    if (seconds < 60L) return "< 1 min"
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}
@Composable
private fun MetadataRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            modifier = Modifier.width(112.dp),
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Text(
            text = value.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
    }
}

@Composable
private fun RemoteArtwork(
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

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun EmptyMessage(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = PocketWhiteMuted,
        )
    }
}
