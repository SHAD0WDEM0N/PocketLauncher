package com.example.pocketlauncher.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.R
import com.example.pocketlauncher.theme.PocketAmber
import com.example.pocketlauncher.theme.PocketGreen
import com.example.pocketlauncher.theme.PocketWhiteDim
import com.example.pocketlauncher.theme.PocketWhiteMuted
import com.example.pocketlauncher.ui.common.pocketLayoutMetrics
import com.example.pocketlauncher.ui.common.PocketAction
import com.example.pocketlauncher.ui.common.PocketActionBar
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeScreen(
    engineReady: Boolean = false,
    selectedIndex: Int = 0,
    menuItems: List<String>,
    manufacturers: Map<String, String> = emptyMap(),
    releaseYears: Map<String, String> = emptyMap(),
    touchMode: Boolean = false,
    onItemClick: (Int) -> Unit = {},
    onIndexChange: (Int) -> Unit = {},
    onSelect: () -> Unit = {},
) {
    val metrics = pocketLayoutMetrics()
    val safeIndex = selectedIndex.coerceIn(0, (menuItems.size - 1).coerceAtLeast(0))
    val selected = menuItems.getOrNull(safeIndex).orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(safeIndex, menuItems.size) {
        if (menuItems.isNotEmpty()) {
            listState.animateScrollToItem(safeIndex)
        }
    }

    LaunchedEffect(listState, safeIndex, menuItems.size) {
        snapshotFlow { listState.isScrollInProgress to listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { (scrolling, index) ->
                if (touchMode && scrolling && menuItems.isNotEmpty() && index in menuItems.indices && index != safeIndex) {
                    onIndexChange(index)
                }
            }
    }


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

        Spacer(Modifier.height(28.dp))

        Text(
            text = "HOME",
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(menuItems) { index, label ->
                ConsoleCard(
                    label = label,
                    selected = index == safeIndex,
                    onClick = { onItemClick(index) },
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        MetadataPanel(
            label = selected,
            manufacturer = manufacturers[selected] ?: specialManufacturer(selected),
            releaseYear = releaseYears[selected] ?: "",
        )

        Spacer(Modifier.weight(1f))

        PocketActionBar(
            touchMode = touchMode,
            actions = listOf(
                PocketAction(
                    controllerLabel = "A  SELECT",
                    touchLabel = "SELECT",
                    onClick = onSelect,
                ),
            ),
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConsoleCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val cardBackground = MaterialTheme.colorScheme.background
    val cardBorder = if (selected) Color.White else PocketWhiteMuted.copy(alpha = 0.36f)
    val foreground = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .width(190.dp)
            .height(182.dp)
            .graphicsLayer {
                scaleX = if (selected) 1.045f else 1f
                scaleY = if (selected) 1.045f else 1f
                shadowElevation = if (selected) 14f else 0f
                shape = RoundedCornerShape(3.dp)
                clip = false
            }
            .clickable(onClick = onClick)
            .background(cardBackground)
            .border(if (selected) 2.dp else 1.dp, cardBorder)
            .padding(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = foreground,
            maxLines = 2,
        )

        Spacer(Modifier.weight(1f))

        if (label == "Game Boy Advance" || label == "Game Boy" || label == "Game Boy Color" || label == "Settings") {
            GeneratedConsoleArt(
                label = label,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(
                        width = when (label) {
                            "Game Boy Advance" -> 138.dp
                            "Settings" -> 118.dp
                            else -> 112.dp
                        },
                        height = when (label) {
                            "Game Boy Advance" -> 104.dp
                            "Settings" -> 112.dp
                            else -> 116.dp
                        },
                    ),
            )
        } else {
            HardwareRender(
                label = label,
                selected = selected,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 124.dp, height = 92.dp),
            )
        }
    }
}

@Composable
private fun GeneratedConsoleArt(
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rawId = when (label) {
        "Game Boy" -> R.raw.gb_console_home_b64
        "Game Boy Color" -> R.raw.gbc_console_home_b64
        "Game Boy Advance" -> R.raw.gba_console_home_b64
        "Settings" -> R.raw.settings_console_home_b64
        else -> return
    }

    val bitmap = remember(label) {
        val encoded = context.resources.openRawResource(rawId)
            .bufferedReader()
            .use { it.readText() }
            .trim()
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val cleaned = if (label == "Game Boy" || label == "Game Boy Color") {
            decoded?.let(::removeEdgeConnectedBackground)
        } else {
            decoded
        }
        cleaned?.asImageBitmap()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "$label console artwork",
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}

private fun removeEdgeConnectedBackground(source: Bitmap): Bitmap {
    val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return bitmap

    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val visited = BooleanArray(pixels.size)
    val queue = IntArray(pixels.size)
    var head = 0
    var tail = 0

    fun isBackground(pixel: Int): Boolean {
        val r = AndroidColor.red(pixel)
        val g = AndroidColor.green(pixel)
        val b = AndroidColor.blue(pixel)
        return r <= 48 && g <= 48 && b <= 48
    }

    fun enqueue(index: Int) {
        if (index !in pixels.indices || visited[index] || !isBackground(pixels[index])) return
        visited[index] = true
        queue[tail++] = index
    }

    for (x in 0 until width) {
        enqueue(x)
        enqueue((height - 1) * width + x)
    }
    for (y in 0 until height) {
        enqueue(y * width)
        enqueue(y * width + width - 1)
    }

    while (head < tail) {
        val index = queue[head++]
        val x = index % width
        val y = index / width
        if (x > 0) enqueue(index - 1)
        if (x + 1 < width) enqueue(index + 1)
        if (y > 0) enqueue(index - width)
        if (y + 1 < height) enqueue(index + width)
    }

    for (i in pixels.indices) {
        if (visited[i]) {
            pixels[i] = pixels[i] and 0x00FFFFFF
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}

@Composable
private fun MetadataPanel(
    label: String,
    manufacturer: String,
    releaseYear: String,
) {
    if (label == "Recently Played" || label == "Settings") {
        MetadataCell(
            heading = "COLLECTION",
            value = if (label == "Recently Played") "RECENT" else "CONFIG",
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(44.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MetadataCell(
            heading = "TYPE",
            value = "PLATFORM",
        )

        MetadataCell(
            heading = "MANUFACTURER",
            value = manufacturer.ifBlank { "POCKETLAUNCHER" }.uppercase(),
        )

        MetadataCell(
            heading = "RELEASED",
            value = releaseYear.ifBlank { "—" },
        )

        MetadataCell(
            heading = "COLLECTION",
            value = label.uppercase(),
        )
    }
}

@Composable
private fun MetadataCell(
    heading: String,
    value: String,
) {
    Column {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelSmall,
            color = PocketWhiteMuted,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun HardwareRender(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val ink = if (selected) Color(0xFF202020) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f)
    val mid = if (selected) Color(0xFF8F8F8B) else PocketWhiteDim.copy(alpha = 0.78f)
    val light = if (selected) Color(0xFFC5C4BE) else PocketWhiteDim.copy(alpha = 0.40f)
    val screen = if (selected) Color(0xFFB8C39B) else PocketGreen.copy(alpha = 0.50f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        fun drawGba() {
            val top = Path().apply {
                moveTo(w * 0.10f, h * 0.34f)
                lineTo(w * 0.80f, h * 0.20f)
                lineTo(w * 0.93f, h * 0.31f)
                lineTo(w * 0.22f, h * 0.47f)
                close()
            }
            drawPath(top, color = light)

            val front = Path().apply {
                moveTo(w * 0.22f, h * 0.47f)
                lineTo(w * 0.93f, h * 0.31f)
                lineTo(w * 0.92f, h * 0.65f)
                lineTo(w * 0.23f, h * 0.80f)
                close()
            }
            drawPath(front, color = mid)

            val side = Path().apply {
                moveTo(w * 0.10f, h * 0.34f)
                lineTo(w * 0.22f, h * 0.47f)
                lineTo(w * 0.23f, h * 0.80f)
                lineTo(w * 0.10f, h * 0.65f)
                close()
            }
            drawPath(side, color = ink.copy(alpha = 0.60f))

            drawRoundRect(
                color = ink,
                topLeft = Offset(w * 0.38f, h * 0.38f),
                size = Size(w * 0.28f, h * 0.20f),
                cornerRadius = CornerRadius(3f),
            )
            drawRect(
                color = screen,
                topLeft = Offset(w * 0.405f, h * 0.405f),
                size = Size(w * 0.23f, h * 0.15f),
            )

            drawRect(ink, Offset(w * 0.24f, h * 0.54f), Size(w * 0.10f, h * 0.025f))
            drawRect(ink, Offset(w * 0.275f, h * 0.50f), Size(w * 0.025f, h * 0.10f))

            drawCircle(ink, radius = h * 0.027f, center = Offset(w * 0.75f, h * 0.47f))
            drawCircle(ink, radius = h * 0.027f, center = Offset(w * 0.80f, h * 0.53f))

            drawRect(ink.copy(alpha = 0.70f), Offset(w * 0.47f, h * 0.66f), Size(w * 0.06f, h * 0.012f))
            drawRect(ink.copy(alpha = 0.70f), Offset(w * 0.56f, h * 0.64f), Size(w * 0.06f, h * 0.012f))

            // Pixel-step highlights make the render read like low-res sprite art.
            drawRect(light.copy(alpha = 0.8f), Offset(w * 0.18f, h * 0.29f), Size(w * 0.08f, h * 0.018f))
            drawRect(light.copy(alpha = 0.6f), Offset(w * 0.70f, h * 0.24f), Size(w * 0.09f, h * 0.018f))
        }

        fun drawGameBoy(colorModel: Boolean) {
            val bodyX = w * 0.34f
            val bodyW = w * 0.34f
            val bodyTop = h * 0.10f
            val bodyH = h * 0.76f

            drawRoundRect(
                color = light,
                topLeft = Offset(bodyX + w * 0.035f, bodyTop - h * 0.025f),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(if (colorModel) 12f else 8f),
            )

            drawRoundRect(
                color = mid,
                topLeft = Offset(bodyX, bodyTop),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(if (colorModel) 12f else 8f),
            )

            drawRoundRect(
                color = ink,
                topLeft = Offset(bodyX + bodyW * 0.11f, bodyTop + bodyH * 0.08f),
                size = Size(bodyW * 0.78f, bodyH * 0.31f),
                cornerRadius = CornerRadius(5f),
            )

            drawRect(
                color = screen,
                topLeft = Offset(bodyX + bodyW * 0.23f, bodyTop + bodyH * 0.135f),
                size = Size(bodyW * 0.54f, bodyH * 0.20f),
            )

            drawRect(ink, Offset(bodyX + bodyW * 0.15f, bodyTop + bodyH * 0.55f), Size(bodyW * 0.25f, bodyH * 0.035f))
            drawRect(ink, Offset(bodyX + bodyW * 0.25f, bodyTop + bodyH * 0.50f), Size(bodyW * 0.05f, bodyH * 0.14f))

            drawCircle(ink, radius = h * 0.024f, center = Offset(bodyX + bodyW * 0.67f, bodyTop + bodyH * 0.56f))
            drawCircle(ink, radius = h * 0.024f, center = Offset(bodyX + bodyW * 0.79f, bodyTop + bodyH * 0.62f))

            for (i in 0..3) {
                drawRect(
                    color = ink.copy(alpha = 0.55f),
                    topLeft = Offset(bodyX + bodyW * (0.58f + i * 0.07f), bodyTop + bodyH * (0.76f + i * 0.008f)),
                    size = Size(bodyW * 0.025f, bodyH * 0.10f),
                )
            }

            drawRect(light.copy(alpha = 0.75f), Offset(bodyX + bodyW * 0.12f, bodyTop + bodyH * 0.02f), Size(bodyW * 0.35f, bodyH * 0.018f))
        }

        when (label) {
            "Game Boy Advance" -> drawGba()
            "Game Boy" -> drawGameBoy(false)
            "Game Boy Color" -> drawGameBoy(true)
            "Recently Played" -> {
                drawRoundRect(
                    color = mid,
                    topLeft = Offset(w * 0.22f, h * 0.22f),
                    size = Size(w * 0.56f, h * 0.52f),
                    cornerRadius = CornerRadius(7f),
                )
                drawRect(ink, Offset(w * 0.30f, h * 0.31f), Size(w * 0.40f, h * 0.25f))
                drawRect(screen, Offset(w * 0.33f, h * 0.34f), Size(w * 0.34f, h * 0.19f))
                drawRect(ink.copy(alpha = 0.65f), Offset(w * 0.45f, h * 0.64f), Size(w * 0.10f, h * 0.018f))
            }
            else -> {
                drawRoundRect(
                    color = mid,
                    topLeft = Offset(w * 0.30f, h * 0.20f),
                    size = Size(w * 0.40f, h * 0.58f),
                    cornerRadius = CornerRadius(7f),
                )
                drawRect(ink, Offset(w * 0.39f, h * 0.31f), Size(w * 0.22f, h * 0.09f))
                drawRect(ink, Offset(w * 0.46f, h * 0.50f), Size(w * 0.08f, h * 0.08f))
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
