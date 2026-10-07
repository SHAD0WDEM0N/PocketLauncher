package com.example.pocketlauncher.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted

data class PocketAction(
    val controllerLabel: String,
    val touchLabel: String,
    val onClick: () -> Unit,
)

@Composable
fun PocketActionBar(
    touchMode: Boolean,
    actions: List<PocketAction>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            val raw = if (touchMode) action.touchLabel else action.controllerLabel
            val parts = raw.trim().split(Regex("\\s+"), limit = 2)
            val badge = if (!touchMode && parts.size == 2 && parts[0].length <= 2) parts[0] else null
            val label = if (badge != null) parts[1] else raw

            Row(
                modifier = Modifier
                    .height(34.dp)
                    .background(Color(0xFF171717), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(10.dp))
                    .focusProperties { canFocus = false }
                    .clickable(onClick = action.onClick)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.White.copy(alpha = 0.92f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF111111),
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.90f),
                )
            }
        }
    }
}

@Composable
fun ConsoleMenuScreen(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    footer: String = "A  SELECT     B  BACK",
    subtitle: String? = null,
    touchMode: Boolean = false,
    primaryControllerLabel: String = "A  SELECT",
    primaryTouchLabel: String = "SELECT",
    secondaryControllerLabel: String? = null,
    secondaryTouchLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    onItemClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val metrics = pocketLayoutMetrics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.merge(
                TextStyle(
                    fontSize = metrics.titleSize,
                    letterSpacing = metrics.titleTracking,
                )
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        if (subtitle != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.merge(
                    TextStyle(fontSize = metrics.secondaryTextSize)
                ),
                color = PocketWhiteMuted,
            )
        }

        Spacer(Modifier.height(metrics.sectionGap))

        items.forEachIndexed { index, label ->
            ConsoleMenuRow(
                label = label,
                selected = index == selectedIndex,
                metrics = metrics,
                onClick = { onItemClick(index) },
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))
        PocketActionBar(
            touchMode = touchMode,
            actions = buildList {
                add(
                    PocketAction(
                        controllerLabel = primaryControllerLabel,
                        touchLabel = primaryTouchLabel,
                        onClick = { onItemClick(selectedIndex) },
                    )
                )
                if (secondaryControllerLabel != null && secondaryTouchLabel != null && onSecondaryAction != null) {
                    add(
                        PocketAction(
                            controllerLabel = secondaryControllerLabel,
                            touchLabel = secondaryTouchLabel,
                            onClick = onSecondaryAction,
                        )
                    )
                }
                add(
                    PocketAction(
                        controllerLabel = "B  BACK",
                        touchLabel = "BACK",
                        onClick = onBack,
                    )
                )
            },
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ConsoleMenuRow(
    label: String,
    selected: Boolean,
    metrics: PocketLayoutMetrics,
    onClick: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.42f,
        animationSpec = tween(120),
        label = "consoleMenuAlpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.rowHeight)
            .clickable(onClick = onClick)
            .alpha(alpha),
    ) {
        Text(
            text = if (selected) ">" else " ",
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(fontSize = metrics.menuTextSize)
            ),
            color = PocketAmber,
            modifier = Modifier.width(32.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(fontSize = metrics.menuTextSize)
            ),
            color = if (selected) MaterialTheme.colorScheme.onBackground else PocketWhiteDim,
            maxLines = 1,
        )
    }
}
