package com.example.pocketlauncher.theme

import android.content.Context
import androidx.compose.ui.graphics.Color

enum class PocketThemeMode(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light");

    fun next(): PocketThemeMode = entries[(ordinal + 1) % entries.size]
    fun previous(): PocketThemeMode = entries[(ordinal - 1 + entries.size) % entries.size]
}

enum class PocketAccentPreset(
    val displayName: String,
    val color: Color,
) {
    AMBER("Amber", Color(0xFFD4A02A)),
    BLUE("Blue", Color(0xFF4EA1FF)),
    GREEN("Green", Color(0xFF46C98B)),
    PURPLE("Purple", Color(0xFF9A7CFF)),
    RED("Red", Color(0xFFE35B52)),
    PINK("Pink", Color(0xFFFF6FAE));

    fun next(): PocketAccentPreset = entries[(ordinal + 1) % entries.size]
    fun previous(): PocketAccentPreset = entries[(ordinal - 1 + entries.size) % entries.size]
}

enum class PocketBackgroundStyle(val displayName: String) {
    SOLID("Solid"),
    PSP_WAVES("PSP Waves"),
    PS2_ORBS("PS2 Orbs"),
    XBOX_GLOW("Xbox Glow"),
    HEX_GRID("Pocket Hex");

    fun next(): PocketBackgroundStyle = entries[(ordinal + 1) % entries.size]
    fun previous(): PocketBackgroundStyle = entries[(ordinal - 1 + entries.size) % entries.size]
}

class ThemePreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("pocketlauncher_theme", Context.MODE_PRIVATE)

    fun themeMode(): PocketThemeMode =
        enumValueOrDefault(prefs.getString(KEY_MODE, null), PocketThemeMode.DARK)

    fun accent(): PocketAccentPreset =
        enumValueOrDefault(prefs.getString(KEY_ACCENT, null), PocketAccentPreset.AMBER)

    fun backgroundStyle(): PocketBackgroundStyle =
        enumValueOrDefault(prefs.getString(KEY_BACKGROUND, null), PocketBackgroundStyle.SOLID)

    fun setThemeMode(value: PocketThemeMode) {
        prefs.edit().putString(KEY_MODE, value.name).apply()
    }

    fun setAccent(value: PocketAccentPreset) {
        prefs.edit().putString(KEY_ACCENT, value.name).apply()
    }

    fun setBackgroundStyle(value: PocketBackgroundStyle) {
        prefs.edit().putString(KEY_BACKGROUND, value.name).apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(raw: String?, fallback: T): T =
        raw?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: fallback

    private companion object {
        const val KEY_MODE = "mode"
        const val KEY_ACCENT = "accent"
        const val KEY_BACKGROUND = "background"
    }
}
