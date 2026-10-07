package com.example.pocketlauncher.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PocketLayoutMetrics(
    val horizontalPadding: Dp,
    val topPadding: Dp,
    val titleSize: TextUnit,
    val titleTracking: TextUnit,
    val menuTextSize: TextUnit,
    val secondaryTextSize: TextUnit,
    val hintTextSize: TextUnit,
    val rowHeight: Dp,
    val sectionGap: Dp,
)

@Composable
fun pocketLayoutMetrics(): PocketLayoutMetrics {
    val config = LocalConfiguration.current
    val width = config.screenWidthDp
    val height = config.screenHeightDp

    return when {
        height < 360 || width < 560 -> PocketLayoutMetrics(
            horizontalPadding = 24.dp,
            topPadding = 24.dp,
            titleSize = 22.sp,
            titleTracking = 4.sp,
            menuTextSize = 17.sp,
            secondaryTextSize = 12.sp,
            hintTextSize = 9.sp,
            rowHeight = 38.dp,
            sectionGap = 20.dp,
        )
        height < 500 || width < 800 -> PocketLayoutMetrics(
            horizontalPadding = 40.dp,
            topPadding = 36.dp,
            titleSize = 30.sp,
            titleTracking = 6.sp,
            menuTextSize = 21.sp,
            secondaryTextSize = 14.sp,
            hintTextSize = 10.sp,
            rowHeight = 48.dp,
            sectionGap = 28.dp,
        )
        else -> PocketLayoutMetrics(
            horizontalPadding = 56.dp,
            topPadding = 48.dp,
            titleSize = 34.sp,
            titleTracking = 7.sp,
            menuTextSize = 23.sp,
            secondaryTextSize = 15.sp,
            hintTextSize = 11.sp,
            rowHeight = 54.dp,
            sectionGap = 34.dp,
        )
    }
}
