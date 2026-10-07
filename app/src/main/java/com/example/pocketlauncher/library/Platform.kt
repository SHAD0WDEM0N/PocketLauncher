package com.example.pocketlauncher.library

/**
 * Supported systems for the first PocketLauncher library milestone.
 *
 * Each platform owns its recognised ROM extensions so scanning logic stays
 * generic as more systems are added later.
 */
enum class Platform(
    val displayName: String,
    val manufacturer: String,
    val releaseYear: String,
    val extensions: Set<String>,
) {
    GB(
        displayName = "Game Boy",
        manufacturer = "Nintendo",
        releaseYear = "1989",
        extensions = setOf("gb"),
    ),
    GBC(
        displayName = "Game Boy Color",
        manufacturer = "Nintendo",
        releaseYear = "1998",
        extensions = setOf("gbc"),
    ),
    GBA(
        displayName = "Game Boy Advance",
        manufacturer = "Nintendo",
        releaseYear = "2001",
        extensions = setOf("gba"),
    ),
}
