package com.pocketlauncher.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.repository.EmulatorAssignment
import com.pocketlauncher.ui.components.FocusableCard
import com.pocketlauncher.viewmodel.HomeViewModel
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemRomListScreen(
    systemName: String,
    homeViewModel: HomeViewModel,
    romViewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val roms by romViewModel.roms.collectAsState()
    val filteredRoms = remember(roms, systemName) {
        roms.filter { it.systemName == systemName }
    }
    val emulatorAssignments by settingsViewModel.emulatorAssignments.collectAsState()
    val context = LocalContext.current

    var romToLaunch by remember { mutableStateOf<RomEntity?>(null) }
    var installedEmulators by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    LaunchedEffect(romToLaunch) {
        if (romToLaunch != null) {
            installedEmulators = settingsViewModel.getInstalledApps().filter { app ->
                // Basic filtering for emulators - can be more sophisticated
                val pkg = app.id.lowercase()
                pkg.contains("emu") || pkg.contains("retroarch") || pkg.contains("ppsspp") || 
                pkg.contains("station") || pkg.contains("ps2") || pkg.contains("dolphin") ||
                pkg.contains("drastic") || pkg.contains("citra") || pkg.contains("yuzu")
            }
        }
    }

    val launchRom: (RomEntity, EmulatorAssignment) -> Unit = { rom, assignment ->
        val pkg = assignment.packageName
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            if (pkg.contains("retroarch")) {
                intent.setAction(android.content.Intent.ACTION_MAIN)
                intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                assignment.retroArchCore?.let { core ->
                    intent.putExtra("LIBRETRO", "/data/data/$pkg/cores/$core")
                }
                intent.putExtra("ROM", rom.filePath)
            } else {
                intent.setData(Uri.parse(rom.filePath))
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(systemName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (filteredRoms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No games found for $systemName", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRoms) { rom ->
                        FocusableCard(
                            onClick = {
                                val assignment = emulatorAssignments[rom.systemName]
                                if (assignment != null) {
                                    launchRom(rom, assignment)
                                    homeViewModel.onRomLaunched(rom)
                                } else {
                                    romToLaunch = rom
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(100.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VideogameAsset,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = rom.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
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
                        items(installedEmulators) { app ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    val assignment = EmulatorAssignment(romToLaunch!!.systemName, app.id)
                                    settingsViewModel.saveEmulatorAssignment(romToLaunch!!.systemName, app.id)
                                    launchRom(romToLaunch!!, assignment)
                                    homeViewModel.onRomLaunched(romToLaunch!!)
                                    romToLaunch = null
                                }.padding(8.dp),
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
                    }
                }
            },
            confirmButton = { TextButton(onClick = { romToLaunch = null }) { Text("Cancel") } }
        )
    }
}
