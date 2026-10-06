package com.example.pocketlauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics

@Composable
fun ScraperSettingsScreen(
    apiKey: String,
    status: String,
    running: Boolean,
    selectedIndex: Int,
    onApiKeyChanged: (String) -> Unit,
    onScrapeNewMissing: () -> Unit,
    onScrapeMissingArtwork: () -> Unit,
    onScrapeMissingMetadata: () -> Unit,
    onRescrapeAll: () -> Unit,
) {
    val metrics = pocketLayoutMetrics()
    val actions = listOf(
        "SCRAPE NEW / MISSING GAMES" to onScrapeNewMissing,
        "SCRAPE MISSING ARTWORK" to onScrapeMissingArtwork,
        "SCRAPE MISSING METADATA" to onScrapeMissingMetadata,
        "RESCRAPE ALL GBA" to onRescrapeAll,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Text(
            text = "ARTWORK & SCRAPING",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "PROVIDER  ·  THEGAMESDB  ·  GBA TESTING",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            label = { Text("TheGamesDB API key") },
            supportingText = { Text("Saved locally. Entering a key does not start scraping.") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        actions.forEachIndexed { index, action ->
            ScrapeActionRow(
                label = action.first,
                selected = index == selectedIndex,
                enabled = apiKey.isNotBlank() && !running,
                onTap = action.second,
            )
            Spacer(Modifier.height(7.dp))
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = when {
                running -> "SCRAPING  ·  $status"
                status.isNotBlank() -> status
                apiKey.isBlank() -> "ADD AN API KEY TO ENABLE SCRAPING"
                else -> "READY  ·  CHOOSE A SCRAPE ACTION"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (running) PocketAmber else MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "New / Missing only targets games with no cached scrape. Missing Artwork and Missing Metadata repair just those gaps. Rescrape All refreshes every configured GBA ROM.",
            style = MaterialTheme.typography.bodySmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "↑ ↓  SELECT    A  SCRAPE    B  BACK",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ScrapeActionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
) {
    val border = when {
        selected -> PocketAmber
        else -> PocketWhiteMuted.copy(alpha = 0.35f)
    }
    val background = if (selected) PocketAmber.copy(alpha = 0.10f) else Color.Transparent
    val textColor = when {
        !enabled -> PocketWhiteMuted.copy(alpha = 0.45f)
        selected -> MaterialTheme.colorScheme.onBackground
        else -> PocketWhiteMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(background)
            .border(1.dp, border)
            .pointerInput(enabled) {
                if (enabled) detectTapGestures(onTap = { onTap() })
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (selected) ">" else " ",
                color = PocketAmber,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(20.dp),
            )
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
