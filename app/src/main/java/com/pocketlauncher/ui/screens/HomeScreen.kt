package com.pocketlauncher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.pocketlauncher.R
import com.pocketlauncher.data.local.RecentGameEntity
import com.pocketlauncher.data.models.AppItem
import com.pocketlauncher.ui.components.FocusableCard
import com.pocketlauncher.viewmodel.AppsViewModel
import com.pocketlauncher.viewmodel.HomeViewModel
import com.pocketlauncher.viewmodel.RomViewModel
import com.pocketlauncher.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    appsViewModel: AppsViewModel,
    romViewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToGames: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToSystem: (String) -> Unit,
    onNavigateToRetroSystems: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val recentRoms by homeViewModel.recentRoms.collectAsState()
    val recentGames by homeViewModel.recentGames.collectAsState()
    val recentApps by homeViewModel.recentApps.collectAsState()
    
    val allGames by appsViewModel.games.collectAsState()
    val allApps by appsViewModel.apps.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Pocket Launcher",
                    modifier = Modifier.height(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        bottomBar = {
            // Xbox 360 style Blade Navigation at the bottom
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 32.dp,
                    containerColor = Color.Black,
                    divider = {},
                    indicator = { tabPositions ->
                        if (tabPositions.isNotEmpty()) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 4.dp
                            )
                        }
                    }
                ) {
                    val pages = listOf("HOME", "RETRO", "GAMES", "APPS", "SETTINGS")
                    pages.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Black else FontWeight.Normal,
                                    color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Dashboard Watermark
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(600.dp)
                    .alpha(0.02f),
                contentScale = ContentScale.Fit
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> HomeDashboardPage(
                        recentRoms = recentRoms,
                        recentGames = recentGames,
                        recentApps = recentApps,
                        onNavigateToSystem = onNavigateToSystem,
                        homeViewModel = homeViewModel
                    )
                    1 -> HorizontalRetroPage(
                        viewModel = romViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateToSystem = onNavigateToSystem
                    )
                    2 -> HorizontalLibraryPage(
                        items = allGames,
                        homeViewModel = homeViewModel
                    )
                    3 -> HorizontalLibraryPage(
                        items = allApps,
                        homeViewModel = homeViewModel
                    )
                    4 -> SettingsScreen(
                        viewModel = settingsViewModel,
                        romViewModel = romViewModel,
                        onNavigateToAbout = onNavigateToAbout,
                        onBack = null
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardPage(
    recentRoms: List<RecentGameEntity>,
    recentGames: List<RecentGameEntity>,
    recentApps: List<RecentGameEntity>,
    onNavigateToSystem: (String) -> Unit,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        if (recentRoms.isNotEmpty()) {
            item {
                ConsoleRow(title = "CONTINUE PLAYING") {
                    items(recentRoms) { game ->
                        ConsoleTile(
                            title = game.title,
                            id = game.id,
                            sourceType = game.sourceType,
                            onClick = { onNavigateToSystem(game.id.substringBefore(':')) }
                        )
                    }
                }
            }
        }

        if (recentGames.isNotEmpty() || recentApps.isNotEmpty()) {
            item {
                ConsoleRow(title = "RECENTLY USED") {
                    items(recentGames) { game ->
                        ConsoleTile(
                            title = game.title,
                            id = game.id,
                            sourceType = game.sourceType,
                            onClick = {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(game.id)
                                launchIntent?.let { context.startActivity(it) }
                                homeViewModel.onGameLaunched(game)
                            }
                        )
                    }
                    items(recentApps) { app ->
                        ConsoleTile(
                            title = app.title,
                            id = app.id,
                            sourceType = app.sourceType,
                            onClick = {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.id)
                                launchIntent?.let { context.startActivity(it) }
                                homeViewModel.onGameLaunched(app)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalLibraryPage(
    items: List<AppItem>,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(items) { app ->
                ConsoleTile(
                    title = app.name,
                    id = app.id,
                    sourceType = if (app.isGame) "ANDROID_GAME" else "ANDROID_APP",
                    isLarge = true,
                    onClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.id)
                        launchIntent?.let { context.startActivity(it) }
                        homeViewModel.onGameLaunched(
                            RecentGameEntity(
                                id = app.id,
                                title = app.name,
                                launchTime = System.currentTimeMillis(),
                                sourceType = if (app.isGame) "ANDROID_GAME" else "ANDROID_APP"
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun HorizontalRetroPage(
    viewModel: RomViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSystem: (String) -> Unit
) {
    val systems by viewModel.systems.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (systems.isEmpty()) {
            Text("No systems detected. Configure in Settings.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(systems) { system ->
                    FocusableCard(
                        onClick = { onNavigateToSystem(system.name) },
                        modifier = Modifier.size(width = 280.dp, height = 360.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = com.pocketlauncher.utils.RetroUtils.getSystemIcon(system.name),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(32.dp))
                            Text(
                                text = system.name.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${system.romCount} GAMES",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsoleRow(
    title: String,
    content: LazyListScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 48.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun ConsoleTile(
    title: String,
    id: String,
    sourceType: String,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = try {
        if (sourceType == "RETRO_ROM") null 
        else context.packageManager.getApplicationIcon(id)
    } catch (e: Exception) {
        null
    }

    val width = if (isLarge) 320.dp else 240.dp
    val height = if (isLarge) 420.dp else 200.dp

    FocusableCard(
        onClick = onClick,
        modifier = Modifier.size(width = width, height = height)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize().alpha(0.2f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap().asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier.size(if (isLarge) 140.dp else 80.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (sourceType == "RETRO_ROM") Icons.Default.VideogameAsset else Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(if (isLarge) 140.dp else 80.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
                
                if (isLarge) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
            
            // Subtle Title Overlay for small tiles
            if (!isLarge) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        .padding(8.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
