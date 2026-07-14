package com.pocketlauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketlauncher.ui.components.FocusableCard
import com.pocketlauncher.utils.RetroUtils
import com.pocketlauncher.viewmodel.RetroSystem
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroSystemsScreen(
    viewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSystem: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val systems by viewModel.systems.collectAsState()
    val emulatorAssignments by settingsViewModel.emulatorAssignments.collectAsState()

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Retro Systems") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(if (onBack != null) padding else PaddingValues(0.dp))) {
            if (systems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No retro systems detected. Add ROM folders in Settings.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(systems) { system ->
                        val assignment = emulatorAssignments[system.name]
                        RetroSystemTile(
                            system = system,
                            assignedEmulator = assignment?.packageName?.substringAfterLast('.'),
                            onClick = { onNavigateToSystem(system.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RetroSystemTile(system: RetroSystem, assignedEmulator: String?, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier.aspectRatio(0.8f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = RetroUtils.getSystemIcon(system.name),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = system.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "${system.romCount} Games",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (assignedEmulator != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = assignedEmulator.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
