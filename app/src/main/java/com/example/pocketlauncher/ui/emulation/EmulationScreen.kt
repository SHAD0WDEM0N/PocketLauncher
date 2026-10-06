package com.example.pocketlauncher.ui.emulation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pocketlauncher.EmulationMenuPage
import com.example.pocketlauncher.engine.MenuHotkey
import com.example.pocketlauncher.engine.VideoFilterMode
import com.example.pocketlauncher.engine.VideoScaleMode

@Composable
fun EmulationScreen(
    menuOpen: Boolean,
    menuPage: EmulationMenuPage,
    menuIndex: Int,
    menuStatus: String,
    scaleMode: VideoScaleMode,
    filterMode: VideoFilterMode,
    menuHotkey: MenuHotkey,
    selectedStateSlot: Int,
    selectedStateSummary: String,
    selectedStateThumbnailPath: String?,
    onScreenMenuIconEnabled: Boolean,
    onMenuIconClick: () -> Unit,
    onMenuItemClick: (Int) -> Unit,
    onStateSlotChange: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AndroidView(
            factory = { context ->
                NativeFrameView(context).apply {
                    this.scaleMode = scaleMode
                    this.filterMode = filterMode
                }
            },
            update = { view ->
                view.scaleMode = scaleMode
                view.filterMode = filterMode
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (onScreenMenuIconEnabled && !menuOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.34f), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onMenuIconClick() })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "≡",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }

        if (menuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f)),
            )

            val title: String
            val items: List<String>
            when (menuPage) {
                EmulationMenuPage.MAIN -> {
                    title = "POCKETLAUNCHER"
                    items = listOf(
                        "Resume",
                        "Save State",
                        "Load State",
                        "State Slot   ◀ ${selectedStateSlot + 1} ▶ · $selectedStateSummary",
                        "Display Settings",
                        "Controller Settings",
                        "Restart Game",
                        "Quit Game",
                    )
                }
                EmulationMenuPage.DISPLAY -> {
                    title = "DISPLAY"
                    items = listOf(
                        "Scaling   ${scaleLabel(scaleMode)}",
                        "Filtering   ${if (filterMode == VideoFilterMode.SHARP) "Sharp" else "Smooth"}",
                        "Back",
                    )
                }
                EmulationMenuPage.CONTROLLER -> {
                    title = "CONTROLLER"
                    items = listOf(
                        "Menu Hotkey   ${hotkeyLabel(menuHotkey)}",
                        "On-screen Menu Icon   ${if (onScreenMenuIconEnabled) "On" else "Off"}",
                        "Back",
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .background(Color.Black.copy(alpha = 0.90f))
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                items.forEachIndexed { index, label ->
                    val selected = index == menuIndex

                    if (menuPage == EmulationMenuPage.MAIN && index == 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.08f)
                                    else Color.Transparent
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "◀",
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = { onStateSlotChange(-1) })
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "State Slot   ${selectedStateSlot + 1} · $selectedStateSummary",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "▶",
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = { onStateSlotChange(1) })
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    } else {
                        Text(
                            text = if (selected) "›  $label" else "   $label",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.08f)
                                    else Color.Transparent
                                )
                                .pointerInput(index) {
                                    detectTapGestures(onTap = { onMenuItemClick(index) })
                                }
                                .padding(vertical = 6.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                            },
                        )
                    }
                }

                if (menuStatus.isNotBlank()) {
                    Text(
                        text = menuStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                    )
                }

                Text(
                    text = "${hotkeyLabel(menuHotkey)}  MENU",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                )
                }

                if (menuPage == EmulationMenuPage.MAIN) {
                    StateThumbnailPreview(
                        path = selectedStateThumbnailPath,
                        slot = selectedStateSlot,
                        modifier = Modifier
                            .weight(1f)
                            .height(250.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StateThumbnailPreview(
    path: String?,
    slot: Int,
    modifier: Modifier = Modifier,
) {
    val image = remember(path) {
        path?.let { filePath ->
            runCatching {
                BitmapFactory.decodeFile(filePath)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Save state slot ${slot + 1} preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = "SLOT ${slot + 1} · NO PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
            )
        }
    }
}
private fun hotkeyLabel(hotkey: MenuHotkey): String = when (hotkey) {
    MenuHotkey.L3_R3 -> "L3 + R3"
    MenuHotkey.START_SELECT -> "START + SELECT"
    MenuHotkey.L1_R1 -> "L1 + R1"
}

private fun scaleLabel(mode: VideoScaleMode): String = when (mode) {
    VideoScaleMode.FIT -> "Fit"
    VideoScaleMode.INTEGER -> "Integer"
    VideoScaleMode.STRETCH -> "Stretch"
}
