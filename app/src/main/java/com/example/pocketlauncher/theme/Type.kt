package com.example.pocketlauncher.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// Using system default sans-serif. For a real release, embed a custom font
// (e.g. DM Sans or Space Grotesk) via res/font and reference it here.
val PocketFontFamily = FontFamily.SansSerif

val Typography = Typography(
    // Large display — used for the POCKETLAUNCHER wordmark
    displayLarge = TextStyle(
        fontFamily = PocketFontFamily,
        fontWeight  = FontWeight.Light,
        fontSize    = 28.sp,
        letterSpacing = 8.sp,
    ),
    // System / section headings
    headlineMedium = TextStyle(
        fontFamily = PocketFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        letterSpacing = 3.sp,
    ),
    // Menu items
    bodyLarge = TextStyle(
        fontFamily = PocketFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    // Labels, status text
    bodySmall = TextStyle(
        fontFamily = PocketFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 11.sp,
        letterSpacing = 1.5.sp,
    ),
    // Engine status badge
    labelSmall = TextStyle(
        fontFamily = PocketFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 10.sp,
        letterSpacing = 2.sp,
    ),
)
