package com.pocketlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // Console style animations: faster response, more dramatic scale
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1f, 
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 24.dp else 2.dp, 
        label = "elevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
        else MaterialTheme.colorScheme.surface,
        label = "color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary 
        else Color.Transparent, 
        label = "border"
    )

    ElevatedCard(
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .shadow(
                elevation = if (isFocused) 30.dp else 0.dp,
                shape = MaterialTheme.shapes.medium,
                spotColor = MaterialTheme.colorScheme.secondary // Blue glow
            )
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = borderColor, // Red border
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation
        )
    ) {
        content()
    }
}
