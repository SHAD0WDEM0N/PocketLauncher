package com.example.pocketlauncher.ui.emulation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.waitForUpOrCancellation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pocketlauncher.EmulationMenuPage
import com.example.pocketlauncher.engine.MenuHotkey
import com.example.pocketlauncher.engine.VideoBorderMode
import com.example.pocketlauncher.engine.VideoEffectMode
import com.example.pocketlauncher.engine.VideoFilterMode
import com.example.pocketlauncher.engine.VideoScaleMode
import com.example.pocketlauncher.input.PocketButton

@Composable
fun EmulationScreen(
    menuOpen: Boolean,
    menuPage: EmulationMenuPage,
    menuIndex: Int,
    menuStatus: String,
    scaleMode: VideoScaleMode,
    filterMode: VideoFilterMode,
    effectMode: VideoEffectMode,
    borderMode: VideoBorderMode,
    platformKey: String,
    menuHotkey: MenuHotkey,
    selectedStateSlot: Int,
    selectedStateSummary: String,
    selectedStateThumbnailPath: String?,
    onScreenMenuIconEnabled: Boolean,
    onScreenControlsEnabled: Boolean,
    physicalControllerInUse: Boolean,
    onMenuIconClick: () -> Unit,
    onTouchControl: (PocketButton, Boolean) -> Unit,
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
                    this.effectMode = effectMode
                    this.borderMode = borderMode
                    this.platformKey = platformKey
                }
            },
            update = { view ->
                view.scaleMode = scaleMode
                view.filterMode = filterMode
                view.effectMode = effectMode
                view.borderMode = borderMode
                view.platformKey = platformKey
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (onScreenControlsEnabled && !physicalControllerInUse && !menuOpen) {
            TouchControlsOverlay(
                onControl = onTouchControl,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (onScreenMenuIconEnabled && !menuOpen) {
            Box(
                modifier = Modifier
                    .align(if (onScreenControlsEnabled && !physicalControllerInUse) Alignment.TopStart else Alignment.BottomStart)
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
                        "Effect   ${effectLabel(effectMode)}",
                        "Border   ${borderLabel(borderMode)}",
                        "Back",
                    )
                }
                EmulationMenuPage.CONTROLLER -> {
                    title = "CONTROLLER"
                    items = listOf(
                        "Menu Hotkey   ${hotkeyLabel(menuHotkey)}",
                        "On-screen Menu Icon   ${if (onScreenMenuIconEnabled) "On" else "Off"}",
                        "On-screen Controls   ${if (onScreenControlsEnabled) "On" else "Off"}",
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
private fun TouchControlsOverlay(
    onControl: (PocketButton, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 30.dp)
                .size(150.dp),
        ) {
            TouchControlButton(
                label = "▲",
                button = PocketButton.UP,
                onControl = onControl,
                modifier = Modifier.align(Alignment.TopCenter).size(54.dp),
            )
            TouchControlButton(
                label = "▼",
                button = PocketButton.DOWN,
                onControl = onControl,
                modifier = Modifier.align(Alignment.BottomCenter).size(54.dp),
            )
            TouchControlButton(
                label = "◀",
                button = PocketButton.LEFT,
                onControl = onControl,
                modifier = Modifier.align(Alignment.CenterStart).size(54.dp),
            )
            TouchControlButton(
                label = "▶",
                button = PocketButton.RIGHT,
                onControl = onControl,
                modifier = Modifier.align(Alignment.CenterEnd).size(54.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 42.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TouchControlButton(
                label = "B",
                button = PocketButton.B,
                onControl = onControl,
                modifier = Modifier.size(66.dp),
            )
            TouchControlButton(
                label = "A",
                button = PocketButton.A,
                onControl = onControl,
                modifier = Modifier.size(66.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TouchControlButton(
                label = "SELECT",
                button = PocketButton.SELECT,
                onControl = onControl,
                modifier = Modifier.width(82.dp).height(36.dp),
                rounded = true,
            )
            TouchControlButton(
                label = "START",
                button = PocketButton.START,
                onControl = onControl,
                modifier = Modifier.width(82.dp).height(36.dp),
                rounded = true,
            )
        }

        TouchControlButton(
            label = "L",
            button = PocketButton.L1,
            onControl = onControl,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 84.dp, top = 18.dp)
                .width(92.dp)
                .height(38.dp),
            rounded = true,
        )

        TouchControlButton(
            label = "R",
            button = PocketButton.R1,
            onControl = onControl,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 28.dp, top = 18.dp)
                .width(92.dp)
                .height(38.dp),
            rounded = true,
        )
    }
}

@Composable
private fun TouchControlButton(
    label: String,
    button: PocketButton,
    onControl: (PocketButton, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    rounded: Boolean = false,
) {
    val shape = if (rounded) RoundedCornerShape(18.dp) else CircleShape
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.34f), shape)
            .pointerInput(button) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onControl(button, true)
                    try {
                        waitForUpOrCancellation()
                    } finally {
                        onControl(button, false)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.76f),
        )
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

private fun borderLabel(mode: VideoBorderMode): String = when (mode) {
    VideoBorderMode.OFF -> "Off"
    VideoBorderMode.AUTO -> "Auto"
}
private fun effectLabel(mode: VideoEffectMode): String = when (mode) {
    VideoEffectMode.OFF -> "Off"
    VideoEffectMode.SCANLINES -> "Scanlines"
    VideoEffectMode.LCD_GRID -> "LCD Grid"
    VideoEffectMode.PIXEL_GRID -> "Pixel Grid"
}
private fun scaleLabel(mode: VideoScaleMode): String = when (mode) {
    VideoScaleMode.FIT -> "Fit"
    VideoScaleMode.INTEGER -> "Integer"
    VideoScaleMode.STRETCH -> "Stretch"
}
