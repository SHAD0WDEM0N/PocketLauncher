package com.example.pocketlauncher.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketGreen
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted

// ─────────────────────────────────────────────────────────────────────────────
// Phase 0 menu items — no artwork, no database yet.
// ─────────────────────────────────────────────────────────────────────────────
private val MENU_ITEMS = listOf(
    "Recently Played",
    "Game Boy",
    "Game Boy Color",
    "Game Boy Advance",
    "Settings",
)

/**
 * HomeScreen — the PocketLauncher home menu.
 *
 * Deliberately minimal in Phase 0: text only, D-pad navigable,
 * styled as a console OS home screen rather than an Android app.
 *
 * @param onNavigateToInputTest  Called when the user opens the input test screen.
 * @param engineReady            True when the JNI / C++ engine has initialised.
 * @param selectedIndex          Currently focused menu item.
 * @param onSelectIndexChanged   Reports focus changes (driven by controller input).
 */
@Composable
fun HomeScreen(
    engineReady: Boolean = false,
    selectedIndex: Int = 0,
    onSelectIndexChanged: (Int) -> Unit = {},
    onNavigateToInputTest: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 48.dp),
    ) {

        Spacer(Modifier.height(64.dp))

        // ── Wordmark ──────────────────────────────────────────────────────────
        Text(
            text  = "POCKETLAUNCHER",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(40.dp))

        // ── Menu ─────────────────────────────────────────────────────────────
        MENU_ITEMS.forEachIndexed { index, label ->
            MenuItem(
                label    = label,
                selected = index == selectedIndex,
                onClick  = {
                    onSelectIndexChanged(index)
                    // In Phase 0, only "Settings" does something (navigate to input test)
                    if (label == "Settings") onNavigateToInputTest()
                },
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.weight(1f))

        // ── Engine status badge ───────────────────────────────────────────────
        EngineStatusBadge(ready = engineReady)

        Spacer(Modifier.height(16.dp))

        // ── Button hint ───────────────────────────────────────────────────────
        Text(
            text  = "A  SELECT",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.45f,
        animationSpec = tween(120),
        label = "menuItemAlpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
    ) {
        // Selection caret
        Text(
            text  = if (selected) ">" else " ",
            style = MaterialTheme.typography.bodyLarge,
            color = PocketAmber,
            modifier = Modifier.width(24.dp),
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onBackground else PocketWhiteDim,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EngineStatusBadge(ready: Boolean) {
    val colour = if (ready) PocketGreen else PocketWhiteMuted
    val label  = if (ready) "ENGINE  READY" else "ENGINE  INIT"

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Status dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(colour, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = colour,
        )
    }
}
