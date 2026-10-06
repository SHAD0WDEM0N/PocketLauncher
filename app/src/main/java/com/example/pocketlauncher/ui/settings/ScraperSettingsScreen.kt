package com.example.pocketlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics

@Composable
fun ScraperSettingsScreen(
    apiKey: String,
    status: String,
    running: Boolean,
    onApiKeyChanged: (String) -> Unit,
) {
    val metrics = pocketLayoutMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = metrics.horizontalPadding),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Text(
            text = "ARTWORK & SCRAPING",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "TEST PROVIDER  ·  THEGAMESDB",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            label = { Text("TheGamesDB API key") },
            supportingText = { Text("Stored only on this device. Paste your own API key.") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = when {
                running -> "SCRAPING  ·  $status"
                status.isNotBlank() -> status
                apiKey.isBlank() -> "NO API KEY CONFIGURED"
                else -> "API KEY READY  ·  OPEN OR RESCAN GBA TO SCRAPE MISSING GAMES"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "GBA currently uses TheGamesDB box art as the label inside the PocketLauncher physical cartridge. ScreenScraper support-2D can replace it later.",
            style = MaterialTheme.typography.bodySmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "B  BACK",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}
