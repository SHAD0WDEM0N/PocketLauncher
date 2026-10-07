package com.example.pocketlauncher.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.pocketlauncher.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PocketBackground(
    style: PocketBackgroundStyle,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        when (style) {
            PocketBackgroundStyle.SOLID -> Unit
            PocketBackgroundStyle.PSP_WAVES -> PspWaveBackground()
            PocketBackgroundStyle.PS2_ORBS -> Ps2OrbBackground()
            PocketBackgroundStyle.XBOX_GLOW -> XboxEnergyBackground()
            PocketBackgroundStyle.HEX_GRID -> PocketHexBackground()
        }
    }
}

@Composable
private fun ThemeArtwork(
    resourceId: Int,
    alpha: Float,
) {
    Image(
        painter = painterResource(resourceId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alpha = alpha,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun PspWaveBackground() {
    val accent = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "pspWaves")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pspWavePhase",
    )

    Box(Modifier.fillMaxSize()) {
        ThemeArtwork(R.drawable.bg_theme_psp, alpha = 0.82f)

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val radians = phase * 6.28318f

            repeat(5) { band ->
                val path = Path()
                val yBase = h * (0.38f + band * 0.055f)
                val amplitude = h * (0.035f + band * 0.004f)
                val speed = 1f + band * 0.10f
                val offset = band * 0.75f

                repeat(121) { index ->
                    val x = w * index / 120f
                    val y = yBase +
                        sin((index / 11f) + radians * speed + offset) * amplitude +
                        sin((index / 24f) - radians * 0.55f + offset) * amplitude * 0.35f

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.34f),
                            accent.copy(alpha = 0.30f),
                            Color.Transparent,
                        ),
                    ),
                    style = Stroke(
                        width = 8f + band * 4f,
                        cap = StrokeCap.Round,
                    ),
                )

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(
                        width = 20f + band * 5f,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Ps2OrbBackground() {
    val accent = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "ps2Orbs")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ps2OrbPhase",
    )

    Box(Modifier.fillMaxSize()) {
        ThemeArtwork(R.drawable.bg_theme_ps2, alpha = 0.76f)

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = phase * 6.28318f
            val minSide = minOf(w, h)

            val orbs = listOf(
                Triple(
                    Offset(
                        w * (0.19f + cos(t) * 0.10f),
                        h * (0.36f + sin(t * 0.72f) * 0.11f),
                    ),
                    minSide * 0.105f,
                    0.25f,
                ),
                Triple(
                    Offset(
                        w * (0.72f + cos(t * 0.74f + 2.1f) * 0.12f),
                        h * (0.31f + sin(t * 0.91f + 1.2f) * 0.13f),
                    ),
                    minSide * 0.145f,
                    0.23f,
                ),
                Triple(
                    Offset(
                        w * (0.53f + cos(t * 1.16f + 4.2f) * 0.16f),
                        h * (0.68f + sin(t * 0.84f + 3.4f) * 0.10f),
                    ),
                    minSide * 0.070f,
                    0.30f,
                ),
                Triple(
                    Offset(
                        w * (0.39f + cos(t * 1.35f + 0.8f) * 0.18f),
                        h * (0.20f + sin(t * 1.12f + 2.8f) * 0.07f),
                    ),
                    minSide * 0.040f,
                    0.34f,
                ),
            )

            orbs.forEach { (center, radius, alpha) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha * 0.72f),
                            accent.copy(alpha = alpha),
                            Color(0xFF11152F).copy(alpha = alpha * 0.88f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            center.x - radius * 0.24f,
                            center.y - radius * 0.28f,
                        ),
                        radius = radius * 1.20f,
                    ),
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.55f),
                    radius = radius * 0.10f,
                    center = Offset(
                        center.x - radius * 0.34f,
                        center.y - radius * 0.38f,
                    ),
                )
                drawCircle(
                    color = accent.copy(alpha = alpha * 0.48f),
                    radius = radius * 1.08f,
                    center = center,
                    style = Stroke(width = 2.2f),
                )
            }

            repeat(3) { line ->
                val y = h * (0.38f + line * 0.12f)
                val path = Path().apply {
                    moveTo(-w * 0.08f, y)
                    cubicTo(
                        w * 0.24f,
                        y - h * (0.13f + line * 0.015f),
                        w * 0.66f,
                        y + h * 0.10f,
                        w * 1.08f,
                        y - h * 0.04f,
                    )
                }
                drawPath(
                    path,
                    accent.copy(alpha = 0.08f + line * 0.015f),
                    style = Stroke(width = 2f + line),
                )
            }
        }
    }
}

@Composable
private fun XboxEnergyBackground() {
    val transition = rememberInfiniteTransition(label = "xboxEnergy")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "xboxEnergyPhase",
    )
    val green = Color(0xFF63FF3E)

    Box(Modifier.fillMaxSize()) {
        ThemeArtwork(R.drawable.bg_theme_xbox, alpha = 0.78f)

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = phase * 6.28318f
            val pulse = 0.72f + ((sin(t) + 1f) * 0.14f)

            val center = Offset(
                w * (0.50f + cos(t * 0.5f) * 0.025f),
                h * (0.58f + sin(t * 0.7f) * 0.018f),
            )
            val radius = minOf(w, h)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        green.copy(alpha = 0.14f * pulse),
                        green.copy(alpha = 0.045f * pulse),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 0.63f,
                ),
                radius = radius * 0.63f,
                center = center,
            )

            repeat(4) { arc ->
                val baseY = h * (0.34f + arc * 0.10f)
                val path = Path().apply {
                    moveTo(-w * 0.08f, baseY + sin(t + arc) * h * 0.04f)
                    cubicTo(
                        w * 0.25f,
                        baseY - h * (0.22f - arc * 0.025f),
                        w * 0.72f,
                        baseY + h * (0.18f - arc * 0.015f),
                        w * 1.08f,
                        baseY + sin(t * 0.7f + arc) * h * 0.05f,
                    )
                }
                drawPath(
                    path = path,
                    color = green.copy(alpha = 0.08f + arc * 0.025f),
                    style = Stroke(
                        width = 3f + arc * 1.3f,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PocketHexBackground() {
    val accent = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "pocketHex")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "hexShift",
    )

    Box(Modifier.fillMaxSize()) {
        ThemeArtwork(R.drawable.bg_theme_hex, alpha = 0.68f)

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cell = minOf(w, h) * 0.105f
            val rowHeight = cell * 0.86f
            val sweepX = (phase * 1.55f - 0.25f) * w

            var row = -2
            var y = -rowHeight
            while (y < h + rowHeight) {
                val rowDirection = if (row % 2 == 0) 1f else -1f
                val movement = phase * cell * 2f * rowDirection
                val stagger = if (row % 2 == 0) 0f else cell * 0.5f
                var x = -cell * 3f + stagger + movement

                while (x < w + cell * 2f) {
                    val r = cell * 0.38f
                    val points = Array(6) { index ->
                        val angle = Math.toRadians((60 * index - 30).toDouble())
                        Offset(
                            x + cos(angle).toFloat() * r,
                            y + sin(angle).toFloat() * r,
                        )
                    }

                    val distanceFromSweep = kotlin.math.abs(x - sweepX)
                    val glow = (1f - (distanceFromSweep / (cell * 3.2f)))
                        .coerceIn(0f, 1f)

                    for (index in points.indices) {
                        drawLine(
                            color = accent.copy(
                                alpha = 0.07f + glow * 0.23f,
                            ),
                            start = points[index],
                            end = points[(index + 1) % points.size],
                            strokeWidth = 1.5f + glow * 2.2f,
                        )
                    }
                    x += cell
                }

                row++
                y += rowHeight
            }

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.02f),
                        accent.copy(alpha = 0.14f),
                        accent.copy(alpha = 0.02f),
                        Color.Transparent,
                    ),
                    startX = sweepX - cell * 4f,
                    endX = sweepX + cell * 4f,
                ),
            )
        }
    }
}
