package com.example.pocketlauncher.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketGreen
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics

@Composable
fun HomeScreen(
    engineReady: Boolean = false,
    selectedIndex: Int = 0,
    menuItems: List<String>,
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
            text = "POCKETLAUNCHER",
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
        Spacer(Modifier.height(metrics.sectionGap))

        menuItems.forEachIndexed { index, label ->
            MenuItem(
                label = label,
                selected = index == selectedIndex,
                rowHeight = metrics.rowHeight,
                textStyle = MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(fontSize = metrics.menuTextSize)
                ),
            )
        }

        Spacer(Modifier.weight(1f))

        EngineStatusBadge(
            ready = engineReady,
            textStyle = MaterialTheme.typography.labelSmall.merge(
                TextStyle(fontSize = metrics.hintTextSize)
            ),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "A  SELECT",
            style = MaterialTheme.typography.labelSmall.merge(
                TextStyle(fontSize = metrics.hintTextSize)
            ),
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MenuItem(
    label: String,
    selected: Boolean,
    rowHeight: androidx.compose.ui.unit.Dp,
    textStyle: TextStyle,
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.42f,
        animationSpec = tween(120),
        label = "menuItemAlpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .alpha(alpha),
    ) {
        Text(
            text = if (selected) ">" else " ",
            style = textStyle,
            color = PocketAmber,
            modifier = Modifier.width(32.dp),
        )

        Text(
            text = label,
            style = textStyle,
            color = if (selected) MaterialTheme.colorScheme.onBackground else PocketWhiteDim,
            maxLines = 1,
        )
    }
}

@Composable
private fun EngineStatusBadge(
    ready: Boolean,
    textStyle: TextStyle,
) {
    val colour = if (ready) PocketGreen else PocketWhiteMuted
    val label = if (ready) "ENGINE  READY" else "ENGINE  INIT"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(colour, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = textStyle,
            color = colour,
        )
    }
}
