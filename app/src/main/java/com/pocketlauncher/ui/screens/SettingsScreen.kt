package com.pocketlauncher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.documentfile.provider.DocumentFile
import com.pocketlauncher.data.local.RomFolderAssignment
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.utils.RetroUtils
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    romViewModel: RomViewModel,
    onNavigateToAbout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val autoScan by viewModel.autoScanStartup.collectAsState()
    val showHidden by viewModel.showHiddenFiles.collectAsState()
    val showAppsHome by viewModel.showAppsHome.collectAsState()
    val showRetroHome by viewModel.showRetroHome.collectAsState()
    val emulatorAssignments by viewModel.emulatorAssignments.collectAsState()
    val folderAssignments by romViewModel.folderAssignments.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showEmulatorPickerFor by remember { mutableStateOf<String?>(null) }
    var showCorePickerFor by remember { mutableStateOf<String?>(null) }
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    
    // Folder Assignment Dialog State
    var pendingFolderAssignment by remember { mutableStateOf<RomFolderAssignment?>(null) }
    var showSystemSelectorForFolder by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val docFile = DocumentFile.fromTreeUri(context, it)
            val folderName = docFile?.name ?: "Unknown"
            val detectedSystem = romViewModel.detectSystem(folderName)
            
            pendingFolderAssignment = RomFolderAssignment(
                folderUri = it.toString(),
                folderName = folderName,
                systemName = detectedSystem ?: "Unknown"
            )
            
            if (detectedSystem == null) {
                showSystemSelectorForFolder = true
            }
        }
    }

    LaunchedEffect(showEmulatorPickerFor, showCorePickerFor) {
        if (showEmulatorPickerFor != null || showCorePickerFor != null) {
            installedApps = viewModel.getInstalledApps()
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(if (onBack != null) padding else PaddingValues(0.dp))
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.pocketlauncher.R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Pocket Launcher", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Version 1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }

            // 1. Appearance
            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsClickableItem(
                    title = "Theme Selection",
                    subtitle = when(themeMode) {
                        "DARK" -> "Dark Mode"
                        "LIGHT" -> "Light Mode"
                        else -> "System Default"
                    },
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // 2. ROM Folders Management
            item { SettingsSectionHeader("ROM Folders") }
            items(folderAssignments) { assignment ->
                ListItem(
                    headlineContent = { Text(assignment.folderName) },
                    supportingContent = { Text("System: ${assignment.systemName}") },
                    leadingContent = { Icon(Icons.Default.Folder, null) },
                    trailingContent = {
                        IconButton(onClick = { romViewModel.deleteFolderAssignment(assignment) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                )
            }
            item {
                Button(
                    onClick = { folderPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add ROM Folder")
                }
            }
            item {
                SettingsSwitchItem(
                    title = "Auto-scan on startup",
                    checked = autoScan,
                    onCheckedChange = { viewModel.setAutoScanStartup(it) },
                    icon = Icons.Default.Sync
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // 3. Emulator Assignments
            item { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSectionHeader("Emulator Assignments")
                    TextButton(onClick = { viewModel.autoConfigureEmulators() }) {
                        Icon(Icons.Default.AutoFixHigh, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Auto Configure")
                    }
                }
            }
            items(viewModel.allSystems) { system ->
                val assignment = emulatorAssignments[system]
                val isRetroArch = assignment?.packageName?.contains("retroarch") == true
                
                ListItem(
                    headlineContent = { Text(system) },
                    supportingContent = {
                        Column {
                            Text(assignment?.packageName ?: "Not assigned (Ask on launch)")
                            if (isRetroArch) {
                                Text(
                                    text = "Core: ${assignment?.retroArchCore ?: "Not set"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    leadingContent = { Icon(Icons.Default.SettingsInputComponent, null) },
                    trailingContent = {
                        Row {
                            if (isRetroArch) {
                                IconButton(onClick = { showCorePickerFor = system }) {
                                    Icon(Icons.Default.Extension, contentDescription = "Select Core")
                                }
                            }
                            IconButton(onClick = { showEmulatorPickerFor = system }) {
                                Icon(Icons.Default.Edit, contentDescription = "Change Emulator")
                            }
                        }
                    }
                )
            }
            item {
                TextButton(onClick = { viewModel.resetEmulatorAssignments() }) {
                    Text("Reset Emulator Assignments")
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // 4. Home Screen
            item { SettingsSectionHeader("Home Screen") }
            item {
                SettingsSwitchItem(
                    title = "Show Apps Collection",
                    checked = showAppsHome,
                    onCheckedChange = { viewModel.setShowAppsHome(it) },
                    icon = Icons.Default.Apps
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Show Retro Collection",
                    checked = showRetroHome,
                    onCheckedChange = { viewModel.setShowRetroHome(it) },
                    icon = Icons.Default.VideogameAsset
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // 5. System
            item { SettingsSectionHeader("System") }
            item {
                SettingsClickableItem(
                    title = "About Pocket Launcher",
                    subtitle = "App info and credits",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
                )
            }
            item {
                SettingsClickableItem(
                    title = "Open System Settings",
                    subtitle = "Android device settings",
                    icon = Icons.Default.Settings,
                    onClick = {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // --- Dialogs ---

    // Folder Confirmation Dialog
    pendingFolderAssignment?.let { assignment ->
        if (!showSystemSelectorForFolder) {
            AlertDialog(
                onDismissRequest = { pendingFolderAssignment = null },
                title = { Text("Assign Folder") },
                text = { Text("Detected system: ${assignment.systemName}\nUse this folder for ${assignment.systemName}?") },
                confirmButton = {
                    TextButton(onClick = {
                        romViewModel.addFolderAssignment(assignment)
                        pendingFolderAssignment = null
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showSystemSelectorForFolder = true }) {
                        Text("Choose System")
                    }
                }
            )
        } else {
            // System Selector Dialog
            AlertDialog(
                onDismissRequest = { showSystemSelectorForFolder = false },
                title = { Text("Select System") },
                text = {
                    Box(Modifier.heightIn(max = 400.dp)) {
                        LazyColumn {
                            items(viewModel.allSystems) { system ->
                                Text(
                                    text = system,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            romViewModel.addFolderAssignment(assignment.copy(systemName = system))
                                            showSystemSelectorForFolder = false
                                            pendingFolderAssignment = null
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }

    // Emulator Picker Dialog
    if (showEmulatorPickerFor != null) {
        val currentSystem = showEmulatorPickerFor!!
        AlertDialog(
            onDismissRequest = { showEmulatorPickerFor = null },
            title = { Text("Select Emulator for $currentSystem") },
            text = {
                Box(Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(installedApps) { app ->
                            AppListItem(app = app) {
                                viewModel.saveEmulatorAssignment(currentSystem, app.id)
                                showEmulatorPickerFor = null
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showEmulatorPickerFor = null }) { Text("Cancel") } }
        )
    }

    // Core Picker Dialog
    if (showCorePickerFor != null) {
        val currentSystem = showCorePickerFor!!
        val recommendedCores = RetroUtils.getRecommendedCores(currentSystem)
        
        AlertDialog(
            onDismissRequest = { showCorePickerFor = null },
            title = { Text("Select core for $currentSystem") },
            text = {
                if (recommendedCores.isEmpty()) {
                    Text("No recommended cores found for this system.")
                } else {
                    Box(Modifier.heightIn(max = 400.dp)) {
                        LazyColumn {
                            items(recommendedCores) { core ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        viewModel.saveRetroArchCore(currentSystem, core.fileName)
                                        showCorePickerFor = null
                                    }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Extension, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(16.dp))
                                    Text(core.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCorePickerFor = null }) { Text("Cancel") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    listOf("SYSTEM" to "System Default", "LIGHT" to "Light Mode", "DARK" to "Dark Mode").forEach { (mode, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                viewModel.setThemeMode(mode)
                                showThemeDialog = false
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = themeMode == mode, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AppListItem(app: AppItem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
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
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
fun SettingsPlaceholderItem(title: String, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text("Coming soon") },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.alpha(0.5f)
    )
}
