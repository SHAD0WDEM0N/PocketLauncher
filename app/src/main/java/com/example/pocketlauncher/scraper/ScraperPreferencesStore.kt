package com.example.pocketlauncher.scraper

import android.content.Context

class ScraperPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("scraper_preferences", Context.MODE_PRIVATE)

    fun theGamesDbApiKey(): String = prefs.getString(KEY_TGDB_API_KEY, "").orEmpty()

    fun setTheGamesDbApiKey(value: String) {
        prefs.edit().putString(KEY_TGDB_API_KEY, value.trim()).apply()
    }

    companion object {
        private const val KEY_TGDB_API_KEY = "thegamesdb_api_key"
    }
}
