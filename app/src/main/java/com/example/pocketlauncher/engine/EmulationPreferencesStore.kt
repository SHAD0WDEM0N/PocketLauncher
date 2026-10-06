package com.example.pocketlauncher.engine

import android.content.Context

enum class VideoScaleMode { FIT, INTEGER, STRETCH }
enum class VideoFilterMode { SHARP, SMOOTH }
enum class VideoEffectMode { OFF, SCANLINES, LCD_GRID, PIXEL_GRID }
enum class VideoBorderMode { OFF, AUTO }
enum class MenuHotkey { L3_R3, START_SELECT, L1_R1 }

class EmulationPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("emulation_preferences", Context.MODE_PRIVATE)

    private fun normalizedKey(platformKey: String): String = platformKey.uppercase()

    fun scaleMode(platformKey: String): VideoScaleMode {
        val key = normalizedKey(platformKey)
        val default = if (key == "GBA") {
            runCatching {
                VideoScaleMode.valueOf(prefs.getString("scale_mode", VideoScaleMode.FIT.name)!!)
            }.getOrDefault(VideoScaleMode.FIT)
        } else {
            VideoScaleMode.FIT
        }
        return runCatching {
            VideoScaleMode.valueOf(
                prefs.getString("scale_mode_$key", default.name)!!
            )
        }.getOrDefault(default)
    }

    fun setScaleMode(platformKey: String, value: VideoScaleMode) {
        prefs.edit().putString("scale_mode_${normalizedKey(platformKey)}", value.name).apply()
    }

    fun filterMode(platformKey: String): VideoFilterMode {
        val key = normalizedKey(platformKey)
        val default = if (key == "GBA") {
            runCatching {
                VideoFilterMode.valueOf(prefs.getString("filter_mode", VideoFilterMode.SHARP.name)!!)
            }.getOrDefault(VideoFilterMode.SHARP)
        } else {
            VideoFilterMode.SHARP
        }
        return runCatching {
            VideoFilterMode.valueOf(
                prefs.getString("filter_mode_$key", default.name)!!
            )
        }.getOrDefault(default)
    }

    fun setFilterMode(platformKey: String, value: VideoFilterMode) {
        prefs.edit().putString("filter_mode_${normalizedKey(platformKey)}", value.name).apply()
    }

    fun effectMode(platformKey: String): VideoEffectMode {
        val key = normalizedKey(platformKey)
        val default = when (key) {
            "GB", "GBC" -> VideoEffectMode.LCD_GRID
            "GBA" -> runCatching {
                VideoEffectMode.valueOf(prefs.getString("effect_mode", VideoEffectMode.OFF.name)!!)
            }.getOrDefault(VideoEffectMode.OFF)
            else -> VideoEffectMode.OFF
        }
        return runCatching {
            VideoEffectMode.valueOf(
                prefs.getString("effect_mode_$key", default.name)!!
            )
        }.getOrDefault(default)
    }

    fun setEffectMode(platformKey: String, value: VideoEffectMode) {
        prefs.edit().putString("effect_mode_${normalizedKey(platformKey)}", value.name).apply()
    }

    fun borderMode(platformKey: String): VideoBorderMode = runCatching {
        VideoBorderMode.valueOf(
            prefs.getString("border_mode_${platformKey.uppercase()}", VideoBorderMode.OFF.name)!!
        )
    }.getOrDefault(VideoBorderMode.OFF)

    fun setBorderMode(platformKey: String, value: VideoBorderMode) {
        prefs.edit().putString("border_mode_${platformKey.uppercase()}", value.name).apply()
    }

    fun menuHotkey(): MenuHotkey = runCatching {
        MenuHotkey.valueOf(prefs.getString("menu_hotkey", MenuHotkey.L3_R3.name)!!)
    }.getOrDefault(MenuHotkey.L3_R3)

    fun setMenuHotkey(value: MenuHotkey) {
        prefs.edit().putString("menu_hotkey", value.name).apply()
    }

    fun onScreenMenuIconEnabled(): Boolean =
        prefs.getBoolean("onscreen_menu_icon", true)

    fun setOnScreenMenuIconEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("onscreen_menu_icon", enabled).apply()
    }
}
