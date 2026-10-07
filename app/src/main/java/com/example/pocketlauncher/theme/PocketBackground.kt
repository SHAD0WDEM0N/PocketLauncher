package com.example.pocketlauncher.theme

import android.graphics.BitmapFactory
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.pocketlauncher.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PocketBackground(
    style: PocketBackgroundStyle,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
    val context = LocalContext.current
    val bitmap = remember(resourceId) {
        runCatching {
            BitmapFactory.decodeResource(context.resources, resourceId)?.asImageBitmap()
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = alpha,
            modifier = Modifier.fillMaxSize(),
        )
    }
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
            if (w <= 0f || h <= 0f) return@Canvas
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
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.34f),
                            accent.copy(alpha = 0.30f),
                            Color.Transparent,
                        ),
                    ),
                    style = Stroke(width = 8f + band * 4f, cap = StrokeCap.Round),
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
            if (w <= 0f || h <= 0f) return@Canvas
            val t = phase * 6.28318f
            val minSide = minOf(w, h)

            val centers = listOf(
                Offset(w * (0.18f + cos(t) * 0.08f), h * (0.34f + sin(t * 0.72f) * 0.09f)),
                Offset(w * (0.73f + cos(t * 0.76f + 2.1f) * 0.10f), h * (0.30f + sin(t * 0.90f + 1.1f) * 0.11f)),
                Offset(w * (0.52f + cos(t * 1.12f + 4.0f) * 0.13f), h * (0.68f + sin(t * 0.82f + 3.2f) * 0.08f)),
                Offset(w * (0.38f + cos(t * 1.28f + 0.7f) * 0.14f), h * (0.20f + sin(t * 1.06f + 2.7f) * 0.06f)),
            )
            val radii = listOf(0.10f, 0.14f, 0.07f, 0.04f)

            centers.forEachIndexed { index, center ->
                val radius = minSide * radii[index]
                drawCircle(
                    color = Color(0xFF6F76FF).copy(alpha = 0.18f),
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = accent.copy(alpha = 0.26f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.4f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f),
                    radius = radius * 0.12f,
                    center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f),
                )
            }

            repeat(3) { line ->
                val y = h * (0.40f + line * 0.11f)
                val path = Path().apply {
                    moveTo(-w * 0.05f, y)
                    cubicTo(
                        w * 0.28f,
                        y - h * 0.10f,
                        w * 0.68f,
                        y + h * 0.08f,
                        w * 1.05f,
                        y - h * 0.03f,
                    )
                }
                drawPath(
                    path = path,
                    color = accent.copy(alpha = 0.07f),
                    style = Stroke(width = 2f + line, cap = StrokeCap.Round),
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
            if (w <= 0f || h <= 0f) return@Canvas
            val t = phase * 6.28318f
            val pulse = 0.68f + ((sin(t) + 1f) * 0.12f)
            val center = Offset(
                w * (0.50f + cos(t * 0.45f) * 0.02f),
                h * (0.58f + sin(t * 0.60f) * 0.02f),
            )
            val radius = minOf(w, h)

            drawCircle(
                color = green.copy(alpha = 0.035f * pulse),
                radius = radius * 0.58f,
                center = center,
            )
            drawCircle(
                color = green.copy(alpha = 0.06f * pulse),
                radius = radius * 0.34f,
                center = center,
                style = Stroke(width = 6f),
            )

            repeat(4) { arc ->
                val baseY = h * (0.34f + arc * 0.10f)
                val path = Path().apply {
                    moveTo(-w * 0.06f, baseY + sin(t + arc) * h * 0.035f)
                    cubicTo(
                        w * 0.25f,
                        baseY - h * (0.18f - arc * 0.02f),
                        w * 0.72f,
                        baseY + h * (0.15f - arc * 0.015f),
                        w * 1.06f,
                        baseY + sin(t * 0.68f + arc) * h * 0.04f,
                    )
                }
                drawPath(
                    path = path,
                    color = green.copy(alpha = 0.07f + arc * 0.018f),
                    style = Stroke(width = 3f + arc, cap = StrokeCap.Round),
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
            if (w <= 0f || h <= 0f) return@Canvas

            val cell = (minOf(w, h) * 0.12f).coerceAtLeast(36f)
            val rowHeight = cell * 0.86f
            val columns = (w / cell).toInt().coerceIn(6, 24) + 4
            val rows = (h / rowHeight).toInt().coerceIn(4, 18) + 4
            val sweepX = phase * (w + cell * 8f) - cell * 4f

            repeat(rows) { rowIndex ->
                val y = (rowIndex - 2) * rowHeight
                val direction = if (rowIndex % 2 == 0) 1f else -1f
                val shift = (phase * cell * 1.8f * direction)
                val stagger = if (rowIndex % 2 == 0) 0f else cell * 0.5f

                repeat(columns) { columnIndex ->
                    val x = (columnIndex - 2) * cell + stagger + shift
                    val radius = cell * 0.36f
                    val path = Path()

                    repeat(6) { pointIndex ->
                        val angle = Math.toRadians((60 * pointIndex - 30).toDouble())
                        val px = x + cos(angle).toFloat() * radius
                        val py = y + sin(angle).toFloat() * radius
                        if (pointIndex == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()

                    val glow = (1f - kotlin.math.abs(x - sweepX) / (cell * 3f))
                        .coerceIn(0f, 1f)
                    drawPath(
                        path = path,
                        color = accent.copy(alpha = 0.07f + glow * 0.18f),
                        style = Stroke(width = 1.4f + glow * 1.8f),
                    )
                }
            }
        }
    }
}
