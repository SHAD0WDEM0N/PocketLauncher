package com.example.pocketlauncher.library

import android.content.Context

class SystemLibraryStore(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getEnabledPlatforms(): Set<Platform> {
        val saved = prefs.getStringSet(KEY_ENABLED, null)
            ?: return setOf(Platform.GBA)

        return saved.mapNotNull { name ->
            runCatching { Platform.valueOf(name) }.getOrNull()
        }.toSet()
    }

    fun setEnabled(platform: Platform, enabled: Boolean): Set<Platform> {
        val updated = getEnabledPlatforms().toMutableSet().apply {
            if (enabled) add(platform) else remove(platform)
        }

        prefs.edit()
            .putStringSet(KEY_ENABLED, updated.map { it.name }.toSet())
            .apply()

        return updated
    }

    private companion object {
        const val PREFS_NAME = "pocketlauncher_systems"
        const val KEY_ENABLED = "enabled_platforms"
    }
}
