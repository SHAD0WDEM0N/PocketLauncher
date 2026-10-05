package com.example.pocketlauncher.engine

import android.util.Log

data class LoadedCoreInfo(
    val name: String,
    val version: String,
    val extensions: String,
)

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
            Log.w(TAG, "Native library not found: ${e.message}")
        }
    }

    fun init(): String? {
        if (!loaded) return null
        return runCatching { nativeInit() }
            .onFailure { Log.e(TAG, "Engine init failed", it) }
            .getOrNull()
    }

    fun getStatus(): String? =
        if (loaded) runCatching { nativeGetStatus() }.getOrNull() else null

    fun loadCore(path: String): Result<LoadedCoreInfo> {
        if (!loaded) return Result.failure(IllegalStateException("Native engine unavailable"))

        return runCatching {
            if (!nativeLoadCore(path)) {
                val error = nativeGetCoreError().ifBlank { "Unknown core loading error" }
                error(error)
            }

            LoadedCoreInfo(
                name = nativeGetCoreName(),
                version = nativeGetCoreVersion(),
                extensions = nativeGetCoreExtensions(),
            )
        }
    }

    fun unloadCore() {
        if (loaded) runCatching { nativeUnloadCore() }
    }

    fun shutdown() {
        if (!loaded) return
        runCatching { nativeShutdown() }
        Log.i(TAG, "Engine shut down")
    }

    private external fun nativeInit(): String
    private external fun nativeShutdown()
    private external fun nativeGetStatus(): String
    private external fun nativeLoadCore(path: String): Boolean
    private external fun nativeUnloadCore()
    private external fun nativeGetCoreName(): String
    private external fun nativeGetCoreVersion(): String
    private external fun nativeGetCoreExtensions(): String
    private external fun nativeGetCoreError(): String
}
