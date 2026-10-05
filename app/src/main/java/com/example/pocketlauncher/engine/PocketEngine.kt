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

    fun loadGame(path: String): Result<Unit> {
        if (!loaded) return Result.failure(IllegalStateException("Native engine unavailable"))
        return runCatching {
            if (!nativeLoadGame(path)) {
                val error = nativeGetCoreError().ifBlank { "ROM load failed" }
                error(error)
            }
        }
    }

    fun loadSaveRam(path: String): Boolean =
        if (loaded) runCatching { nativeLoadSaveRam(path) }.getOrDefault(false) else false

    fun saveSaveRam(path: String): Boolean =
        if (loaded) runCatching { nativeSaveSaveRam(path) }.getOrDefault(false) else false

    fun unloadGame() { if (loaded) runCatching { nativeUnloadGame() } }

    fun runFrame(): Boolean = if (loaded) runCatching { nativeRunFrame() }.getOrDefault(false) else false
    fun frameWidth(): Int = if (loaded) runCatching { nativeGetFrameWidth() }.getOrDefault(0) else 0
    fun frameHeight(): Int = if (loaded) runCatching { nativeGetFrameHeight() }.getOrDefault(0) else 0
    fun frameCount(): Long = if (loaded) runCatching { nativeGetFrameCount() }.getOrDefault(0L) else 0L
    fun copyFrameRgba(): IntArray = if (loaded) runCatching { nativeCopyFrameRgba() }.getOrDefault(IntArray(0)) else IntArray(0)

    fun setInputMask(mask: Int) {
        if (loaded) runCatching { nativeSetInputMask(mask) }
    }

    fun videoFps(): Double =
        if (loaded) runCatching { nativeGetVideoFps() }.getOrDefault(60.0) else 60.0

    fun audioSampleRate(): Double =
        if (loaded) runCatching { nativeGetAudioSampleRate() }.getOrDefault(0.0) else 0.0

    fun drainAudio(): ShortArray =
        if (loaded) runCatching { nativeDrainAudio() }.getOrDefault(ShortArray(0)) else ShortArray(0)

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
    private external fun nativeLoadGame(path: String): Boolean
    private external fun nativeUnloadGame()
    private external fun nativeRunFrame(): Boolean
    private external fun nativeGetFrameWidth(): Int
    private external fun nativeGetFrameHeight(): Int
    private external fun nativeGetFrameCount(): Long
    private external fun nativeCopyFrameRgba(): IntArray
    private external fun nativeSetInputMask(mask: Int)
    private external fun nativeGetAudioSampleRate(): Double
    private external fun nativeDrainAudio(): ShortArray
    private external fun nativeGetVideoFps(): Double
    private external fun nativeLoadSaveRam(path: String): Boolean
    private external fun nativeSaveSaveRam(path: String): Boolean
}
