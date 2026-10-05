package com.example.pocketlauncher.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
    val safeIndex = selectedIndex.coerceIn(0, (menuItems.size - 1).coerceAtLeast(0))
    val selected = menuItems.getOrNull(safeIndex).orEmpty()
    val previous = menuItems.getOrNull(safeIndex - 1)
    val next = menuItems.getOrNull(safeIndex + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
        Spacer(Modifier.height(metrics.topPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "POCKETLAUNCHER",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = metrics.titleTracking,
            )

            Spacer(Modifier.weight(1f))

            EngineStatusBadge(
                ready = engineReady,
                textStyle = MaterialTheme.typography.labelSmall.merge(
                    TextStyle(fontSize = metrics.hintTextSize)
                ),
            )
        }

        Spacer(Modifier.weight(0.55f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            SideLabel(
                text = previous ?: "",
                alignment = TextAlign.End,
                modifier = Modifier.width(150.dp),
            )

            Spacer(Modifier.width(18.dp))

            SystemCard(
                label = selected,
                modifier = Modifier.width(320.dp),
            )

            Spacer(Modifier.width(18.dp))

            SideLabel(
                text = next ?: "",
                alignment = TextAlign.Start,
                modifier = Modifier.width(150.dp),
            )
        }

        Spacer(Modifier.weight(0.45f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            menuItems.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == safeIndex) 18.dp else 8.dp)
                        .background(
                            if (index == safeIndex) PocketAmber else PocketWhiteMuted.copy(alpha = 0.35f)
                        ),
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "◀  ▶  BROWSE    A  OPEN",
            style = MaterialTheme.typography.labelSmall.merge(
                TextStyle(fontSize = metrics.hintTextSize)
            ),
            color = PocketWhiteMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SystemCard(
    label: String,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(140),
        label = "systemCardAlpha",
    )

    Column(
        modifier = modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HardwareGlyph(
            label = label,
            modifier = Modifier.size(width = 230.dp, height = 150.dp),
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = systemSubtitle(label),
            style = MaterialTheme.typography.labelMedium,
            color = PocketWhiteDim,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SideLabel(
    text: String,
    alignment: TextAlign,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier.alpha(if (text.isBlank()) 0f else 0.26f),
        style = MaterialTheme.typography.titleSmall,
        color = PocketWhiteDim,
        textAlign = alignment,
        maxLines = 2,
    )
}

@Composable
private fun HardwareGlyph(
    label: String,
    modifier: Modifier = Modifier,
) {
    val body = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
    val detail = MaterialTheme.colorScheme.background
    val screen = PocketGreen.copy(alpha = 0.42f)
    val accent = PocketAmber.copy(alpha = 0.82f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (label) {
            "Game Boy Advance" -> {
                drawRect(
                    color = body,
                    topLeft = Offset(w * 0.08f, h * 0.22f),
                    size = Size(w * 0.84f, h * 0.58f),
                )
                drawRect(
                    color = detail,
                    topLeft = Offset(w * 0.31f, h * 0.30f),
                    size = Size(w * 0.38f, h * 0.34f),
                )
                drawRect(
                    color = screen,
                    topLeft = Offset(w * 0.34f, h * 0.33f),
                    size = Size(w * 0.32f, h * 0.28f),
                )

                drawRect(color = detail, topLeft = Offset(w * 0.17f, h * 0.43f), size = Size(w * 0.13f, h * 0.05f))
                drawRect(color = detail, topLeft = Offset(w * 0.21f, h * 0.37f), size = Size(w * 0.05f, h * 0.17f))
                drawRect(color = accent, topLeft = Offset(w * 0.76f, h * 0.39f), size = Size(w * 0.055f, h * 0.075f))
                drawRect(color = accent, topLeft = Offset(w * 0.82f, h * 0.48f), size = Size(w * 0.055f, h * 0.075f))
                drawRect(color = body.copy(alpha = 0.55f), topLeft = Offset(w * 0.12f, h * 0.17f), size = Size(w * 0.23f, h * 0.05f))
                drawRect(color = body.copy(alpha = 0.55f), topLeft = Offset(w * 0.65f, h * 0.17f), size = Size(w * 0.23f, h * 0.05f))
            }

            "Game Boy", "Game Boy Color" -> {
                val bodyX = if (label == "Game Boy") w * 0.31f else w * 0.33f
                val bodyW = if (label == "Game Boy") w * 0.38f else w * 0.34f
                drawRect(
                    color = body,
                    topLeft = Offset(bodyX, h * 0.07f),
                    size = Size(bodyW, h * 0.86f),
                )
                drawRect(
                    color = detail,
                    topLeft = Offset(bodyX + bodyW * 0.13f, h * 0.16f),
                    size = Size(bodyW * 0.74f, h * 0.31f),
                )
                drawRect(
                    color = screen,
                    topLeft = Offset(bodyX + bodyW * 0.19f, h * 0.20f),
                    size = Size(bodyW * 0.62f, h * 0.22f),
                )

                drawRect(color = detail, topLeft = Offset(bodyX + bodyW * 0.17f, h * 0.59f), size = Size(bodyW * 0.25f, h * 0.055f))
                drawRect(color = detail, topLeft = Offset(bodyX + bodyW * 0.27f, h * 0.54f), size = Size(bodyW * 0.055f, h * 0.16f))
                drawRect(color = accent, topLeft = Offset(bodyX + bodyW * 0.63f, h * 0.56f), size = Size(bodyW * 0.09f, h * 0.07f))
                drawRect(color = accent, topLeft = Offset(bodyX + bodyW * 0.75f, h * 0.62f), size = Size(bodyW * 0.09f, h * 0.07f))

                for (i in 0..3) {
                    drawRect(
                        color = detail.copy(alpha = 0.55f),
                        topLeft = Offset(bodyX + bodyW * (0.57f + i * 0.07f), h * 0.78f),
                        size = Size(bodyW * 0.035f, h * 0.07f),
                    )
                }
            }

            "Recently Played" -> {
                drawRect(
                    color = body,
                    topLeft = Offset(w * 0.21f, h * 0.20f),
                    size = Size(w * 0.58f, h * 0.58f),
                )
                drawRect(
                    color = detail,
                    topLeft = Offset(w * 0.28f, h * 0.27f),
                    size = Size(w * 0.44f, h * 0.34f),
                )
                drawRect(
                    color = screen,
                    topLeft = Offset(w * 0.31f, h * 0.30f),
                    size = Size(w * 0.38f, h * 0.28f),
                )
                drawRect(color = accent, topLeft = Offset(w * 0.45f, h * 0.66f), size = Size(w * 0.10f, h * 0.04f))
            }

            else -> {
                val block = w * 0.10f
                drawRect(color = body, topLeft = Offset(w * 0.28f, h * 0.20f), size = Size(w * 0.44f, h * 0.58f))
                drawRect(color = detail, topLeft = Offset(w * 0.37f, h * 0.30f), size = Size(w * 0.26f, h * 0.11f))
                drawRect(color = accent, topLeft = Offset(w * 0.45f, h * 0.49f), size = Size(block, block))
                drawRect(color = detail, topLeft = Offset(w * 0.39f, h * 0.64f), size = Size(w * 0.22f, h * 0.04f))
            }
        }
    }
}

private fun systemSubtitle(label: String): String = when (label) {
    "Recently Played" -> "PLAY HISTORY"
    "Game Boy" -> "8-BIT HANDHELD"
    "Game Boy Color" -> "COLOR HANDHELD"
    "Game Boy Advance" -> "32-BIT HANDHELD"
    "Settings" -> "SYSTEM & FRONTEND"
    else -> "LIBRARY"
}

@Composable
private fun EngineStatusBadge(
    ready: Boolean,
    textStyle: TextStyle,
) {
    val colour = if (ready) PocketGreen else PocketWhiteMuted
    val label = if (ready) "ENGINE READY" else "ENGINE INIT"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(colour),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = textStyle,
            color = colour,
        )
    }
}
