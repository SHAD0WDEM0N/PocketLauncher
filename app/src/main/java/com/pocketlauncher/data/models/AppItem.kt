package com.pocketlauncher.data.models

import android.graphics.drawable.Drawable

data class AppItem(
    val id: String, // package name
    val name: String,
    val icon: Drawable?,
    val isGame: Boolean
)
