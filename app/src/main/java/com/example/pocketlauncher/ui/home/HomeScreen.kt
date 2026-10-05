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
import androidx.compose.ui.geometry.CornerRadius
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
    manufacturers: Map<String, String> = emptyMap(),
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

        Spacer(Modifier.weight(0.48f))

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
                manufacturer = manufacturers[selected] ?: specialManufacturer(selected),
                modifier = Modifier.width(340.dp),
            )

            Spacer(Modifier.width(18.dp))

            SideLabel(
                text = next ?: "",
                alignment = TextAlign.Start,
                modifier = Modifier.width(150.dp),
            )
        }

        Spacer(Modifier.weight(0.42f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            menuItems.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == safeIndex) 16.dp else 7.dp)
                        .background(
                            if (index == safeIndex) PocketAmber else PocketWhiteMuted.copy(alpha = 0.30f)
                        ),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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
    manufacturer: String,
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
            modifier = Modifier.size(width = 250.dp, height = 165.dp),
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = if (label == "Recently Played") "LIBRARY" else if (label == "Settings") "SYSTEM" else "PLATFORM",
            style = MaterialTheme.typography.labelSmall,
            color = PocketAmber.copy(alpha = 0.78f),
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = manufacturer.uppercase(),
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
        modifier = modifier.alpha(if (text.isBlank()) 0f else 0.22f),
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
    val shell = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.84f)
    val shadow = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.20f)
    val dark = MaterialTheme.colorScheme.background
    val screen = PocketGreen.copy(alpha = 0.34f)
    val accent = PocketAmber.copy(alpha = 0.82f)
    val soft = PocketWhiteDim.copy(alpha = 0.55f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        fun scanlines(x: Float, y: Float, width: Float, height: Float, count: Int = 6) {
            val gap = height / (count + 1)
            for (i in 1..count) {
                drawRect(
                    color = dark.copy(alpha = 0.16f),
                    topLeft = Offset(x, y + gap * i),
                    size = Size(width, 1.5f),
                )
            }
        }

        fun pixelDust(x: Float, y: Float) {
            val p = 3.5f
            drawRect(soft, Offset(x, y), Size(p, p))
            drawRect(soft.copy(alpha = 0.35f), Offset(x + p * 2.2f, y + p * 1.4f), Size(p, p))
            drawRect(soft.copy(alpha = 0.22f), Offset(x + p * 4.0f, y - p * 0.8f), Size(p, p))
        }

        when (label) {
            "Game Boy Advance" -> {
                drawRoundRect(
                    color = shadow,
                    topLeft = Offset(w * 0.075f, h * 0.245f),
                    size = Size(w * 0.86f, h * 0.56f),
                    cornerRadius = CornerRadius(h * 0.18f),
                )
                drawRoundRect(
                    color = shell,
                    topLeft = Offset(w * 0.06f, h * 0.22f),
                    size = Size(w * 0.88f, h * 0.56f),
                    cornerRadius = CornerRadius(h * 0.18f),
                )
                drawRoundRect(
                    color = dark,
                    topLeft = Offset(w * 0.285f, h * 0.275f),
                    size = Size(w * 0.43f, h * 0.36f),
                    cornerRadius = CornerRadius(8f),
                )
                drawRect(
                    color = screen,
                    topLeft = Offset(w * 0.32f, h * 0.31f),
                    size = Size(w * 0.36f, h * 0.28f),
                )
                scanlines(w * 0.32f, h * 0.31f, w * 0.36f, h * 0.28f)

                // D-pad
                drawRoundRect(dark, Offset(w * 0.155f, h * 0.405f), Size(w * 0.16f, h * 0.055f), CornerRadius(4f))
                drawRoundRect(dark, Offset(w * 0.207f, h * 0.35f), Size(w * 0.055f, h * 0.17f), CornerRadius(4f))

                // A/B
                drawCircle(accent, radius = h * 0.052f, center = Offset(w * 0.795f, h * 0.40f))
                drawCircle(accent.copy(alpha = 0.70f), radius = h * 0.052f, center = Offset(w * 0.855f, h * 0.49f))

                // Start / Select
                drawRoundRect(dark.copy(alpha = 0.72f), Offset(w * 0.445f, h * 0.67f), Size(w * 0.07f, h * 0.018f), CornerRadius(3f))
                drawRoundRect(dark.copy(alpha = 0.72f), Offset(w * 0.535f, h * 0.67f), Size(w * 0.07f, h * 0.018f), CornerRadius(3f))

                // Shoulder hints
                drawRoundRect(shell.copy(alpha = 0.58f), Offset(w * 0.12f, h * 0.17f), Size(w * 0.20f, h * 0.055f), CornerRadius(5f))
                drawRoundRect(shell.copy(alpha = 0.58f), Offset(w * 0.68f, h * 0.17f), Size(w * 0.20f, h * 0.055f), CornerRadius(5f))
                pixelDust(w * 0.10f, h * 0.83f)
            }

            "Game Boy" -> {
                val x = w * 0.31f
                val bw = w * 0.38f
                drawRoundRect(
                    color = shadow,
                    topLeft = Offset(x + w * 0.012f, h * 0.065f),
                    size = Size(bw, h * 0.84f),
                    cornerRadius = CornerRadius(14f),
                )
                drawRoundRect(
                    color = shell,
                    topLeft = Offset(x, h * 0.045f),
                    size = Size(bw, h * 0.84f),
                    cornerRadius = CornerRadius(14f),
                )
                drawRoundRect(
                    color = dark.copy(alpha = 0.90f),
                    topLeft = Offset(x + bw * 0.09f, h * 0.12f),
                    size = Size(bw * 0.82f, h * 0.34f),
                    cornerRadius = CornerRadius(9f),
                )
                drawRect(screen, Offset(x + bw * 0.22f, h * 0.17f), Size(bw * 0.58f, h * 0.22f))
                scanlines(x + bw * 0.22f, h * 0.17f, bw * 0.58f, h * 0.22f, 5)
                drawCircle(accent.copy(alpha = 0.70f), radius = 3.5f, center = Offset(x + bw * 0.15f, h * 0.30f))

                drawRoundRect(dark, Offset(x + bw * 0.13f, h * 0.57f), Size(bw * 0.28f, h * 0.052f), CornerRadius(3f))
                drawRoundRect(dark, Offset(x + bw * 0.235f, h * 0.515f), Size(bw * 0.055f, h * 0.17f), CornerRadius(3f))
                drawCircle(accent, radius = h * 0.044f, center = Offset(x + bw * 0.67f, h * 0.56f))
                drawCircle(accent.copy(alpha = 0.72f), radius = h * 0.044f, center = Offset(x + bw * 0.79f, h * 0.63f))
                drawRoundRect(dark.copy(alpha = 0.68f), Offset(x + bw * 0.34f, h * 0.72f), Size(bw * 0.13f, h * 0.018f), CornerRadius(3f))
                drawRoundRect(dark.copy(alpha = 0.68f), Offset(x + bw * 0.52f, h * 0.72f), Size(bw * 0.13f, h * 0.018f), CornerRadius(3f))

                for (i in 0..4) {
                    drawRoundRect(
                        color = dark.copy(alpha = 0.50f),
                        topLeft = Offset(x + bw * (0.61f + i * 0.055f), h * (0.78f + i * 0.008f)),
                        size = Size(bw * 0.035f, h * 0.075f),
                        cornerRadius = CornerRadius(2f),
                    )
                }
                pixelDust(x - w * 0.07f, h * 0.89f)
            }

            "Game Boy Color" -> {
                val x = w * 0.325f
                val bw = w * 0.35f
                drawRoundRect(
                    color = shadow,
                    topLeft = Offset(x + w * 0.012f, h * 0.065f),
                    size = Size(bw, h * 0.82f),
                    cornerRadius = CornerRadius(20f),
                )
                drawRoundRect(
                    color = shell,
                    topLeft = Offset(x, h * 0.045f),
                    size = Size(bw, h * 0.82f),
                    cornerRadius = CornerRadius(20f),
                )
                drawRoundRect(
                    color = dark.copy(alpha = 0.90f),
                    topLeft = Offset(x + bw * 0.08f, h * 0.12f),
                    size = Size(bw * 0.84f, h * 0.31f),
                    cornerRadius = CornerRadius(10f),
                )
                drawRect(screen.copy(alpha = 0.44f), Offset(x + bw * 0.19f, h * 0.165f), Size(bw * 0.62f, h * 0.20f))
                scanlines(x + bw * 0.19f, h * 0.165f, bw * 0.62f, h * 0.20f, 5)

                drawRoundRect(dark, Offset(x + bw * 0.13f, h * 0.55f), Size(bw * 0.27f, h * 0.05f), CornerRadius(3f))
                drawRoundRect(dark, Offset(x + bw * 0.235f, h * 0.50f), Size(bw * 0.055f, h * 0.16f), CornerRadius(3f))
                drawCircle(accent, radius = h * 0.043f, center = Offset(x + bw * 0.66f, h * 0.54f))
                drawCircle(accent.copy(alpha = 0.72f), radius = h * 0.043f, center = Offset(x + bw * 0.79f, h * 0.61f))
                drawRoundRect(dark.copy(alpha = 0.65f), Offset(x + bw * 0.34f, h * 0.70f), Size(bw * 0.13f, h * 0.017f), CornerRadius(3f))
                drawRoundRect(dark.copy(alpha = 0.65f), Offset(x + bw * 0.52f, h * 0.70f), Size(bw * 0.13f, h * 0.017f), CornerRadius(3f))
                for (i in 0..4) {
                    drawRoundRect(
                        color = dark.copy(alpha = 0.46f),
                        topLeft = Offset(x + bw * (0.62f + i * 0.052f), h * (0.77f + i * 0.006f)),
                        size = Size(bw * 0.032f, h * 0.065f),
                        cornerRadius = CornerRadius(2f),
                    )
                }
                pixelDust(x - w * 0.06f, h * 0.88f)
            }

            "Recently Played" -> {
                drawRoundRect(
                    color = shell,
                    topLeft = Offset(w * 0.20f, h * 0.19f),
                    size = Size(w * 0.60f, h * 0.58f),
                    cornerRadius = CornerRadius(12f),
                )
                drawRoundRect(
                    color = dark,
                    topLeft = Offset(w * 0.27f, h * 0.27f),
                    size = Size(w * 0.46f, h * 0.30f),
                    cornerRadius = CornerRadius(6f),
                )
                drawRect(screen, Offset(w * 0.30f, h * 0.30f), Size(w * 0.40f, h * 0.24f))
                scanlines(w * 0.30f, h * 0.30f, w * 0.40f, h * 0.24f, 5)
                drawRoundRect(accent, Offset(w * 0.44f, h * 0.65f), Size(w * 0.12f, h * 0.025f), CornerRadius(3f))
                pixelDust(w * 0.14f, h * 0.82f)
            }

            else -> {
                drawRoundRect(
                    color = shell,
                    topLeft = Offset(w * 0.27f, h * 0.18f),
                    size = Size(w * 0.46f, h * 0.60f),
                    cornerRadius = CornerRadius(12f),
                )
                drawRoundRect(dark, Offset(w * 0.36f, h * 0.29f), Size(w * 0.28f, h * 0.12f), CornerRadius(5f))
                drawRect(accent, Offset(w * 0.46f, h * 0.49f), Size(w * 0.08f, h * 0.08f))
                drawRoundRect(dark.copy(alpha = 0.62f), Offset(w * 0.40f, h * 0.66f), Size(w * 0.20f, h * 0.025f), CornerRadius(3f))
                pixelDust(w * 0.20f, h * 0.83f)
            }
        }
    }
}

private fun specialManufacturer(label: String): String = when (label) {
    "Recently Played" -> "PocketLauncher"
    "Settings" -> "PocketLauncher"
    else -> ""
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
