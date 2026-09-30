package com.example.pocketlauncher.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.input.PocketButton
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketBorder
import com.example.pocketlauncher.theme.PocketGreen
import com.example.pocketlauncher.theme.PocketSurface
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted

// ─────────────────────────────────────────────────────────────────────────────
// InputTestScreen — Phase 0 controller diagnostic
//
// Shows which PocketButton events arrive from the connected hardware.
// Acts as the verification that the physical gamepad is correctly mapped
// before we build any real UI navigation on top of it.
// ─────────────────────────────────────────────────────────────────────────────

data class ButtonEvent(
    val button: PocketButton,
    val pressed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)

@Composable
fun InputTestScreen(
    events: List<ButtonEvent>,
    currentlyHeld: Set<PocketButton>,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest event
    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) listState.scrollToItem(events.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text  = "INPUT TEST",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Press any button on the controller",
            style = MaterialTheme.typography.bodySmall,
            color = PocketWhiteDim,
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = PocketBorder, thickness = 1.dp)
        Spacer(Modifier.height(24.dp))

        // ── Live button state grid ────────────────────────────────────────────
        Text(
            text  = "CURRENTLY HELD",
            style = MaterialTheme.typography.headlineMedium,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(12.dp))

        ButtonGrid(currentlyHeld = currentlyHeld)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = PocketBorder, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // ── Event log ─────────────────────────────────────────────────────────
        Text(
            text  = "EVENT LOG",
            style = MaterialTheme.typography.headlineMedium,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state    = listState,
            modifier = Modifier.weight(1f),
        ) {
            items(events) { event ->
                EventLogRow(event = event)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text  = "B  BACK",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ButtonGrid(currentlyHeld: Set<PocketButton>) {
    val rows = listOf(
        listOf(PocketButton.UP, PocketButton.DOWN, PocketButton.LEFT, PocketButton.RIGHT),
        listOf(PocketButton.A, PocketButton.B, PocketButton.X, PocketButton.Y),
        listOf(PocketButton.L1, PocketButton.R1, PocketButton.L2, PocketButton.R2),
        listOf(PocketButton.START, PocketButton.SELECT, PocketButton.MENU),
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { button ->
                    val held = button in currentlyHeld
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = if (held) PocketAmber else PocketSurface,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            )
                            .border(
                                width = 1.dp,
                                color = if (held) PocketAmber else PocketBorder,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text  = button.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (held) MaterialTheme.colorScheme.onPrimary
                                    else PocketWhiteDim,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EventLogRow(event: ButtonEvent) {
    val stateLabel = if (event.pressed) "DOWN" else "UP  "
    val stateColor = if (event.pressed) PocketGreen else PocketWhiteDim

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text     = stateLabel,
            style    = MaterialTheme.typography.labelSmall,
            color    = stateColor,
            modifier = Modifier.width(44.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text  = event.button.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}
