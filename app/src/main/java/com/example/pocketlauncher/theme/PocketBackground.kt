package com.example.pocketlauncher.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PocketBackground(
    style: PocketBackgroundStyle,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        if (style == PocketBackgroundStyle.SOLID) return@Box

        val transition = rememberInfiniteTransition(label = "pocketBackground")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(12_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "backgroundPhase",
        )

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (style) {
                PocketBackgroundStyle.SOLID -> Unit
                PocketBackgroundStyle.PSP_WAVES -> {
                    repeat(5) { row ->
                        val yBase = h * (0.22f + row * 0.13f)
                        val alpha = 0.11f - row * 0.012f
                        var previous: Offset? = null
                        repeat(91) { index ->
                            val x = w * index / 90f
                            val radians = (index / 9f) + phase * 6.28318f + row * 0.85f
                            val y = yBase + sin(radians) * h * (0.035f + row * 0.006f)
                            val current = Offset(x, y)
                            previous?.let {
                                drawLine(
                                    color = accent.copy(alpha = alpha.coerceAtLeast(0.035f)),
                                    start = it,
                                    end = current,
                                    strokeWidth = 2.2f,
                                )
                            }
                            previous = current
                        }
                    }
                }
                PocketBackgroundStyle.PS2_ORBS -> {
                    val orbit = phase * 6.28318f
                    val centers = listOf(
                        Offset(w * (0.30f + cos(orbit) * 0.08f), h * (0.34f + sin(orbit) * 0.08f)),
                        Offset(w * (0.67f + cos(orbit + 2.1f) * 0.09f), h * (0.55f + sin(orbit + 2.1f) * 0.07f)),
                        Offset(w * (0.48f + cos(orbit + 4.2f) * 0.06f), h * (0.77f + sin(orbit + 4.2f) * 0.05f)),
                    )
                    centers.forEachIndexed { index, center ->
                        val radius = minOf(w, h) * (0.13f + index * 0.025f)
                        drawCircle(accent.copy(alpha = 0.035f), radius * 1.9f, center)
                        drawCircle(accent.copy(alpha = 0.055f), radius * 1.35f, center)
                        drawCircle(accent.copy(alpha = 0.08f), radius, center, style = Stroke(width = 2f))
                    }
                }
                PocketBackgroundStyle.XBOX_GLOW -> {
                    val pulse = 0.5f + (sin(phase * 6.28318f) + 1f) * 0.25f
                    val center = Offset(
                        w * (0.72f + cos(phase * 6.28318f) * 0.035f),
                        h * 0.48f,
                    )
                    val radius = minOf(w, h)
                    drawCircle(accent.copy(alpha = 0.018f * pulse), radius * 0.90f, center)
                    drawCircle(accent.copy(alpha = 0.035f * pulse), radius * 0.62f, center)
                    drawCircle(accent.copy(alpha = 0.060f * pulse), radius * 0.34f, center)
                    drawCircle(accent.copy(alpha = 0.10f * pulse), radius * 0.13f, center)
                }
                PocketBackgroundStyle.HEX_GRID -> {
                    val cell = minOf(w, h) * 0.085f
                    val rowHeight = cell * 0.86f
                    val xOffset = phase * cell
                    var row = -2
                    var y = -rowHeight
                    while (y < h + rowHeight) {
                        val stagger = if (row % 2 == 0) 0f else cell * 0.5f
                        var x = -cell * 2 + stagger + xOffset
                        while (x < w + cell) {
                            val r = cell * 0.38f
                            val pts = Array(6) { i ->
                                val angle = Math.toRadians((60 * i - 30).toDouble())
                                Offset(
                                    x + cos(angle).toFloat() * r,
                                    y + sin(angle).toFloat() * r,
                                )
                            }
                            for (i in pts.indices) {
                                drawLine(
                                    color = accent.copy(alpha = 0.075f),
                                    start = pts[i],
                                    end = pts[(i + 1) % pts.size],
                                    strokeWidth = 1.3f,
                                )
                            }
                            x += cell
                        }
                        row++
                        y += rowHeight
                    }
                }
            }
        }
    }
}
