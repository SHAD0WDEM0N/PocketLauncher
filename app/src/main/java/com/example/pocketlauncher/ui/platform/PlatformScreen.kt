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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.library.GameEntry
import com.example.pocketlauncher.library.Platform
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics

@Composable
fun PlatformScreen(
    platform: Platform,
    folderLabel: String?,
    games: List<GameEntry>,
    selectedIndex: Int,
    isScanning: Boolean,
) {
    val listState = rememberLazyListState()
    val metrics = pocketLayoutMetrics()

    LaunchedEffect(selectedIndex, games.size) {
        if (games.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex.coerceIn(games.indices))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Text(
            text = platform.displayName.uppercase(),
            style = MaterialTheme.typography.displayLarge.merge(
                TextStyle(
                    fontSize = metrics.titleSize,
                    letterSpacing = metrics.titleTracking,
                )
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        if (folderLabel != null) {
            Text(
                text = "FOLDER  $folderLabel",
                style = MaterialTheme.typography.labelSmall.merge(
                    TextStyle(fontSize = metrics.hintTextSize)
                ),
                color = PocketWhiteMuted,
            )
        }

        Spacer(Modifier.height(metrics.sectionGap))

        when {
            folderLabel == null -> {
                EmptyMessage(
                    title = "No ROM folder configured.",
                    subtitle = "Press A to select a folder for ${platform.displayName}.",
                    menuSize = metrics.menuTextSize,
                    secondarySize = metrics.secondaryTextSize,
                )
            }

            isScanning -> {
                EmptyMessage(
                    title = "Scanning...",
                    subtitle = "Looking for ${platform.extensions.joinToString(" / ") { ".$it" }} files.",
                    menuSize = metrics.menuTextSize,
                    secondarySize = metrics.secondaryTextSize,
                )
            }

            games.isEmpty() -> {
                EmptyMessage(
                    title = "No games found.",
                    subtitle = "Press Y to rescan or X to choose a different folder.",
                    menuSize = metrics.menuTextSize,
                    secondarySize = metrics.secondaryTextSize,
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
                            rowHeight = metrics.rowHeight,
                            textSize = metrics.menuTextSize,
                        )
                    }
                }
            }
        }

        if (games.isEmpty()) {
            Spacer(Modifier.weight(1f))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        Text(
            text = if (folderLabel == null) {
                "A  SELECT FOLDER     B  BACK"
            } else {
                "A  SELECT     X  CHANGE FOLDER     Y  RESCAN     B  BACK"
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
private fun GameRow(
    game: GameEntry,
    selected: Boolean,
    rowHeight: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .alpha(if (selected) 1f else 0.42f),
    ) {
        Text(
            text = if (selected) ">" else " ",
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(fontSize = textSize)
            ),
            color = PocketAmber,
            modifier = Modifier.width(32.dp),
        )

        Text(
            text = game.displayName,
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(fontSize = textSize)
            ),
            color = if (selected) MaterialTheme.colorScheme.onBackground else PocketWhiteDim,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptyMessage(
    title: String,
    subtitle: String,
    menuSize: androidx.compose.ui.unit.TextUnit,
    secondarySize: androidx.compose.ui.unit.TextUnit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(fontSize = menuSize)
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.merge(
                TextStyle(fontSize = secondarySize)
            ),
            color = PocketWhiteMuted,
        )
    }
}
