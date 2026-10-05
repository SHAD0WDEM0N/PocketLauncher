package com.example.pocketlauncher.ui.emulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                        "State Slot   ${selectedStateSlot + 1} · $selectedStateSummary",
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
                        "Back",
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 28.dp)
                    .width(330.dp)
                    .background(Color.Black.copy(alpha = 0.90f))
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                items.forEachIndexed { index, label ->
                    Text(
                        text = if (index == menuIndex) "›  $label" else "   $label",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (index == menuIndex) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f)
                        },
                    )
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
