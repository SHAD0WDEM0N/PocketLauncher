package com.example.pocketlauncher.library

/**
 * Supported systems for the first PocketLauncher library milestone.
 *
 * Each platform owns its recognised ROM extensions so scanning logic stays
 * generic as more systems are added later.
 */
enum class Platform(
    val displayName: String,
    val extensions: Set<String>,
) {
    GB(
        displayName = "Game Boy",
        extensions = setOf("gb"),
    ),
    GBC(
        displayName = "Game Boy Color",
        extensions = setOf("gbc"),
    ),
    GBA(
        displayName = "Game Boy Advance",
        extensions = setOf("gba"),
    ),
}
