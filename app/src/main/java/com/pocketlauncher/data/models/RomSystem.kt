package com.pocketlauncher.data.models

data class RomSystem(
    val name: String,
    val emulatorPackage: String,
    val supportedExtensions: List<String>
)
