package com.example.pocketlauncher.ui.emulation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.pocketlauncher.theme.PocketWhiteMuted

@Composable
fun EmulationScreen(
    title: String,
    status: String,
    pixels: IntArray,
    width: Int,
    height: Int,
) {
    val bitmap = remember(pixels, width, height) {
        if (width > 0 && height > 0 && pixels.size == width * height) {
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            if (bitmap == null) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
            }

            Text(
                text = status.ifBlank { "Waiting for first frame..." },
                style = MaterialTheme.typography.labelSmall,
                color = PocketWhiteMuted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "B  EXIT",
                style = MaterialTheme.typography.labelSmall,
                color = PocketWhiteMuted,
            )
        }
    }
}
