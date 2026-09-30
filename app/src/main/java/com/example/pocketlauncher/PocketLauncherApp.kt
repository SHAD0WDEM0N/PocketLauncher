package com.example.pocketlauncher

import android.app.Application
import android.util.Log
import com.example.pocketlauncher.engine.PocketEngine

/**
 * PocketLauncherApp — Application subclass.
 *
 * Initialises the native engine as early as possible so that the
 * "ENGINE READY" badge in the home screen reflects truth before the
 * first frame renders.
 */
class PocketLauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val version = PocketEngine.init()
        if (version != null) {
            Log.i("PocketLauncherApp", "Engine ready — version $version")
        } else {
            Log.w("PocketLauncherApp", "Engine not available — Phase 0 UI-only mode")
        }
    }

    override fun onTerminate() {
        PocketEngine.shutdown()
        super.onTerminate()
    }
}
