package com.example.pocketlauncher.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted

/**
 * Minimal controller-first platform library.
 *
 * No folder configured:
 *   A opens Android's folder picker.
 *
 * Folder configured:
 *   UP/DOWN moves through ROMs, X changes the folder, Y rescans, B returns home.
 */
@Composable
fun PlatformScreen(
    platform: Platform,
    folderLabel: String?,
    games: List<GameEntry>,
    selectedIndex: Int,
    isScanning: Boolean,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex, games.size) {
        if (games.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex.coerceIn(games.indices))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 48.dp),
    ) {
        Spacer(Modifier.height(64.dp))

        Text(
            text = platform.displayName.uppercase(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        if (folderLabel != null) {
            Text(
                text = "FOLDER  $folderLabel",
                style = MaterialTheme.typography.labelSmall,
                color = PocketWhiteMuted,
            )
        }

        Spacer(Modifier.height(28.dp))

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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                ) {
                    itemsIndexed(
                        items = games,
                        key = { _, game -> game.uri },
                    ) { index, game ->
                        GameRow(
                            game = game,
                            selected = index == selectedIndex,
                        )
                    }
                }
            }
        }

        if (games.isEmpty()) {
            Spacer(Modifier.weight(1f))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(14.dp))

        Text(
            text = if (folderLabel == null) {
                "A  SELECT FOLDER     B  BACK"
            } else {
                "A  SELECT     X  CHANGE FOLDER     Y  RESCAN     B  BACK"
            },
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun GameRow(
    game: GameEntry,
    selected: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .alpha(if (selected) 1f else 0.45f),
    ) {
        Text(
            text = if (selected) ">" else " ",
            style = MaterialTheme.typography.bodyLarge,
            color = PocketAmber,
            modifier = Modifier.width(32.dp),
        )

        Text(
            text = game.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onBackground else PocketWhiteDim,
            maxLines = 1,
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
            style = MaterialTheme.typography.bodyMedium,
            color = PocketWhiteMuted,
        )
    }
}
