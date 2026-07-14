package com.pocketlauncher.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class EmulatorAssignment(
    val systemName: String,
    val packageName: String,
    val retroArchCore: String? = null
)

class SettingsRepository(private val context: Context) {

    private val dataStore = context.dataStore

    object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, DARK, LIGHT
        val AUTO_SCAN_STARTUP = booleanPreferencesKey("auto_scan_startup")
        val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")
        val INCLUDE_SUBFOLDERS = booleanPreferencesKey("include_subfolders")
        val RECENT_GAMES_COUNT = intPreferencesKey("recent_games_count")
        val SHOW_APPS_HOME = booleanPreferencesKey("show_apps_home")
        val SHOW_RETRO_HOME = booleanPreferencesKey("show_retro_home")
        
        fun emulatorKey(systemName: String) = stringPreferencesKey("emulator_$systemName")
        fun coreKey(systemName: String) = stringPreferencesKey("core_$systemName")
    }

    val themeMode: Flow<String> = dataStore.data.map { it[PreferencesKeys.THEME_MODE] ?: "SYSTEM" }
    val autoScanStartup: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.AUTO_SCAN_STARTUP] ?: true }
    val showHiddenFiles: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_HIDDEN_FILES] ?: false }
    val includeSubfolders: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.INCLUDE_SUBFOLDERS] ?: true }
    val recentGamesCount: Flow<Int> = dataStore.data.map { it[PreferencesKeys.RECENT_GAMES_COUNT] ?: 10 }
    val showAppsHome: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_APPS_HOME] ?: true }
    val showRetroHome: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SHOW_RETRO_HOME] ?: true }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setAutoScanStartup(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.AUTO_SCAN_STARTUP] = enabled }
    }
    
    suspend fun setShowHiddenFiles(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_HIDDEN_FILES] = enabled }
    }
    
    suspend fun setIncludeSubfolders(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.INCLUDE_SUBFOLDERS] = enabled }
    }
    
    suspend fun setRecentGamesCount(count: Int) {
        dataStore.edit { it[PreferencesKeys.RECENT_GAMES_COUNT] = count }
    }
    
    suspend fun setShowAppsHome(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_APPS_HOME] = show }
    }
    
    suspend fun setShowRetroHome(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_RETRO_HOME] = show }
    }

    fun getEmulatorAssignment(systemName: String): Flow<String?> {
        return dataStore.data.map { it[PreferencesKeys.emulatorKey(systemName)] }
    }

    suspend fun saveEmulatorAssignment(systemName: String, packageName: String) {
        dataStore.edit { it[PreferencesKeys.emulatorKey(systemName)] = packageName }
    }

    fun getRetroArchCore(systemName: String): Flow<String?> {
        return dataStore.data.map { it[PreferencesKeys.coreKey(systemName)] }
    }

    suspend fun saveRetroArchCore(systemName: String, corePath: String?) {
        dataStore.edit { 
            if (corePath != null) {
                it[PreferencesKeys.coreKey(systemName)] = corePath
            } else {
                it.remove(PreferencesKeys.coreKey(systemName))
            }
        }
    }

    suspend fun getAllEmulatorAssignments(): Map<String, EmulatorAssignment> {
        val prefs = dataStore.data.first()
        val assignments = mutableMapOf<String, EmulatorAssignment>()
        
        prefs.asMap().keys.forEach { key ->
            if (key.name.startsWith("emulator_")) {
                val systemName = key.name.removePrefix("emulator_")
                val packageName = prefs[key] as? String
                if (packageName != null) {
                    val core = prefs[PreferencesKeys.coreKey(systemName)]
                    assignments[systemName] = EmulatorAssignment(systemName, packageName, core)
                }
            }
        }
        return assignments
    }

    suspend fun resetEmulatorAssignments() {
        dataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { 
                it.name.startsWith("emulator_") || it.name.startsWith("core_") 
            }
            keysToRemove.forEach { prefs.remove(it) }
        }
    }
}
