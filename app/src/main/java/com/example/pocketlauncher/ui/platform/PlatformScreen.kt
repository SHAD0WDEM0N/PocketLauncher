package com.example.pocketlauncher.ui.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.R
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
            listState.scrollToItem(safeIndex)
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
                "◀  ▶  BROWSE    A  PLAY    X  OPTIONS    Y  FAVOURITE    B  BACK"
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
    title: String = "Recently Played",
    emptyTitle: String = "No recent games yet.",
    emptySubtitle: String = "Launch and quit a game to add it here.",
    showFavouriteAction: Boolean = false,
) {
    val metrics = pocketLayoutMetrics()
    val listState = rememberLazyListState()
    val safeIndex = selectedIndex.coerceIn(0, (games.size - 1).coerceAtLeast(0))
    val selectedGame = games.getOrNull(safeIndex)

    LaunchedEffect(safeIndex, games.size) {
        if (games.isNotEmpty()) listState.scrollToItem(safeIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))

        when {
            isScanning -> EmptyMessage("Loading...", "Reading recent play history.")
            games.isEmpty() -> EmptyMessage(emptyTitle, emptySubtitle)
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
            text = if (showFavouriteAction) {
                "◀  ▶  BROWSE    A  PLAY    X  OPTIONS    Y  UNFAVOURITE    B  BACK"
            } else {
                "◀  ▶  BROWSE    A  PLAY    X  OPTIONS    B  BACK"
            },
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
    val outerBackground = if (selected) Color(0xFFEDEAE3) else Color(0xFF181818)
    val outerBorder = if (selected) Color.White else Color(0xFF2A2A2A)

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(170.dp)
            .background(outerBackground)
            .border(1.dp, outerBorder),
        contentAlignment = Alignment.Center,
    ) {
        ArtworkCard(
            game = game,
            selected = selected,
        )
    }
}

@Composable
private fun ArtworkCard(
    game: GameEntry,
    selected: Boolean,
) {
    var aspectRatio by remember(game.artworkUrl) { mutableStateOf(0.78f) }

    LaunchedEffect(game.artworkUrl) {
        val url = game.artworkUrl ?: return@LaunchedEffect
        val cached = ArtworkMemoryCache.get(url)
        if (cached != null && cached.height > 0) {
            aspectRatio = cached.width.toFloat() / cached.height.toFloat()
        } else {
            val decoded = withContext(Dispatchers.IO) { decodeArtwork(url) }
            if (decoded != null && decoded.height > 0) {
                aspectRatio = decoded.width.toFloat() / decoded.height.toFloat()
            }
        }
    }

    val maxHeight = 146.dp
    val maxWidth = 118.dp
    val safeAspect = aspectRatio.coerceIn(0.58f, 0.95f)

    val targetWidth: Dp
    val targetHeight: Dp
    if (maxHeight * safeAspect <= maxWidth) {
        targetHeight = maxHeight
        targetWidth = maxHeight * safeAspect
    } else {
        targetWidth = maxWidth
        targetHeight = maxWidth / safeAspect
    }

    Box(
        modifier = Modifier
            .width(targetWidth)
            .height(targetHeight)
            .offset(y = if (selected) (-3).dp else 0.dp)
            .graphicsLayer {
                scaleX = if (selected) 1.035f else 1f
                scaleY = if (selected) 1.035f else 1f
                shadowElevation = if (selected) 14f else 7f
                shape = RoundedCornerShape(4.dp)
                clip = false
            }
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.White else Color(0xFF444444),
                shape = RoundedCornerShape(4.dp),
            )
            .clip(RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (game.artworkUrl != null) {
            RemoteArtwork(
                url = game.artworkUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = game.displayName.uppercase(),
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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

private object ArtworkMemoryCache {
    private val cache = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(url: String): Bitmap? = cache.get(url)

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }
}

private fun decodeArtwork(url: String): Bitmap? {
    ArtworkMemoryCache.get(url)?.let { return it }
    return runCatching {
        val bytes = URL(url).openStream().use { it.readBytes() }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 320 || bounds.outHeight / sample > 320) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample.coerceAtLeast(1)
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.also {
            ArtworkMemoryCache.put(url, it)
        }
    }.getOrNull()
}
@Composable
private fun RemoteArtwork(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var bitmap by remember(url) {
        mutableStateOf(ArtworkMemoryCache.get(url)?.asImageBitmap())
    }

    LaunchedEffect(url) {
        if (bitmap == null) {
            bitmap = withContext(Dispatchers.IO) {
                decodeArtwork(url)?.asImageBitmap()
            }
        }
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
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
