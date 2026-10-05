package com.example.pocketlauncher.engine

import android.content.Context

enum class VideoScaleMode { FIT, INTEGER, STRETCH }
enum class VideoFilterMode { SHARP, SMOOTH }
enum class MenuHotkey { L3_R3, START_SELECT, L1_R1 }

class EmulationPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("emulation_preferences", Context.MODE_PRIVATE)

    fun scaleMode(): VideoScaleMode = runCatching {
        VideoScaleMode.valueOf(prefs.getString("scale_mode", VideoScaleMode.FIT.name)!!)
    }.getOrDefault(VideoScaleMode.FIT)

    fun setScaleMode(value: VideoScaleMode) {
        prefs.edit().putString("scale_mode", value.name).apply()
    }

    fun filterMode(): VideoFilterMode = runCatching {
        VideoFilterMode.valueOf(prefs.getString("filter_mode", VideoFilterMode.SHARP.name)!!)
    }.getOrDefault(VideoFilterMode.SHARP)

    fun setFilterMode(value: VideoFilterMode) {
        prefs.edit().putString("filter_mode", value.name).apply()
    }

    fun menuHotkey(): MenuHotkey = runCatching {
        MenuHotkey.valueOf(prefs.getString("menu_hotkey", MenuHotkey.L3_R3.name)!!)
    }.getOrDefault(MenuHotkey.L3_R3)

    fun setMenuHotkey(value: MenuHotkey) {
        prefs.edit().putString("menu_hotkey", value.name).apply()
    }
}
