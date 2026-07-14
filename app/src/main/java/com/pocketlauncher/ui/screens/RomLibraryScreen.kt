package com.pocketlauncher.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.pocketlauncher.data.local.RomEntity
import com.pocketlauncher.ui.components.FocusableCard
import com.pocketlauncher.utils.RetroUtils
import com.pocketlauncher.viewmodel.RetroSystem
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.repository.EmulatorAssignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomLibraryScreen(
    viewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val roms by viewModel.roms.collectAsState()
    val systems by viewModel.systems.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val emulatorAssignments by settingsViewModel.emulatorAssignments.collectAsState()
    val context = LocalContext.current

    var selectedSystem by remember { mutableStateOf<String?>(null) }
    var romToLaunch by remember { mutableStateOf<RomEntity?>(null) }
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    LaunchedEffect(romToLaunch) {
        if (romToLaunch != null) {
            installedApps = settingsViewModel.getInstalledApps()
        }
    }

    val launchRom: (RomEntity, EmulatorAssignment) -> Unit = { rom, assignment ->
        val pkg = assignment.packageName
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            if (pkg.contains("retroarch")) {
                // RetroArch specialized intent
                intent.setAction(android.content.Intent.ACTION_MAIN)
                intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                assignment.retroArchCore?.let { core ->
                    intent.putExtra("LIBRETRO", "/data/data/$pkg/cores/$core")
                }
                intent.putExtra("ROM", rom.filePath)
            } else {
                // Generic or known standalone behavior
                intent.setData(Uri.parse(rom.filePath))
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedSystem ?: "Retro Collection") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedSystem != null) selectedSystem = null else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isScanning && selectedSystem == null) {
                        IconButton(onClick = { viewModel.scanLibrary() }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Scan Library")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isScanning) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning for Games...")
                }
            } else if (systems.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No retro games found.", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add ROM folders in Settings to begin.", style = MaterialTheme.typography.bodySmall)
                }
            } else if (selectedSystem == null) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(systems) { system ->
                        val assignment = emulatorAssignments[system.name]
                        SystemCard(
                            system = system,
                            assignedEmulator = assignment?.packageName?.substringAfterLast('.'),
                            onClick = { selectedSystem = system.name }
                        )
                    }
                }
            } else {
                val filteredRoms = roms.filter { it.systemName == selectedSystem }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRoms) { rom ->
                        RomCard(rom = rom, onClick = {
                            val assignment = emulatorAssignments[rom.systemName]
                            if (assignment != null) {
                                launchRom(rom, assignment)
                            } else {
                                romToLaunch = rom
                            }
                        })
                    }
                }
            }
        }
    }

    if (romToLaunch != null) {
        AlertDialog(
            onDismissRequest = { romToLaunch = null },
            title = { Text("Choose Emulator for ${romToLaunch!!.systemName}") },
            text = {
                Box(Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(installedApps) { app ->
                            SettingsAppListItem(app = app) {
                                val assignment = EmulatorAssignment(romToLaunch!!.systemName, app.id)
                                settingsViewModel.saveEmulatorAssignment(romToLaunch!!.systemName, app.id)
                                launchRom(romToLaunch!!, assignment)
                                romToLaunch = null
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { romToLaunch = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsAppListItem(app: AppItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        app.icon?.let {
            androidx.compose.foundation.Image(
                bitmap = it.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(app.name, style = MaterialTheme.typography.bodyLarge)
            Text(app.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SystemCard(system: RetroSystem, assignedEmulator: String?, onClick: () -> Unit) {
    FocusableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = RetroUtils.getSystemIcon(system.name),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = system.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(text = "${system.romCount} Games", style = MaterialTheme.typography.bodySmall)
            if (assignedEmulator != null) {
                Text(text = assignedEmulator, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun RomCard(rom: RomEntity, onClick: () -> Unit) {
    FocusableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(100.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.VideogameAsset,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            }
            Text(text = rom.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
