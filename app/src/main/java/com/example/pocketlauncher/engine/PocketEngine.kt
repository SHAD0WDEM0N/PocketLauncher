package com.example.pocketlauncher.engine

import android.util.Log

/**
 * PocketEngine — Kotlin-side JNI bridge to the native C++ layer.
 *
 * Lifecycle:
 *   Call [init] when the application starts (Application.onCreate or MainActivity.onCreate).
 *   Call [shutdown] in onDestroy.
 *
 * Phase 0: proves the JNI chain works. The status result drives the "ENGINE READY"
 *          badge in the home screen.
 * Phase 2: this class will grow to expose core loading, ROM booting, etc.
 */
object PocketEngine {

    private const val TAG = "PocketEngine"
    private const val LIBRARY = "pocket-engine"

    private var loaded = false

    init {
        try {
            System.loadLibrary(LIBRARY)
            loaded = true
            Log.i(TAG, "Native library '$LIBRARY' loaded")
        } catch (e: UnsatisfiedLinkError) {
            // This happens on desktop / CI where the .so isn't present.
            // Phase 0 gracefully degrades so the UI still launches.
            Log.w(TAG, "Native library not found — running without engine: ${e.message}")
        }
    }

    /**
     * Initialise the native engine.
     * @return Engine version string from C++ on success, null on failure.
     */
    fun init(): String? {
        if (!loaded) return null
        return try {
            val version = nativeInit()
            Log.i(TAG, "Engine initialised — version $version")
            version
        } catch (e: Exception) {
            Log.e(TAG, "Engine init failed: ${e.message}")
            null
        }
    }

    /**
     * Query the engine status for the UI badge.
     * @return "READY", "NOT_INIT", or null if the library isn't loaded.
     */
    fun getStatus(): String? {
        if (!loaded) return null
        return try {
            nativeGetStatus()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Shut down the native engine cleanly.
     */
    fun shutdown() {
        if (!loaded) return
        try {
            nativeShutdown()
            Log.i(TAG, "Engine shut down")
        } catch (e: Exception) {
            Log.e(TAG, "Engine shutdown error: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // Native declarations — implemented in pocket_engine.cpp
    // -------------------------------------------------------------------------

    private external fun nativeInit(): String
    private external fun nativeShutdown()
    private external fun nativeGetStatus(): String
}
