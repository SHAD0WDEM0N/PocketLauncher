#include "pocket_engine.h"
#include <android/log.h>
#include <string>

#define LOG_TAG "PocketEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ─────────────────────────────────────────────────────────────────────────────
// Phase 0 — proof of concept JNI implementation
//
// All this does in Phase 0 is confirm that:
//   Android → Kotlin → JNI → C++ → logcat
// works correctly on both target devices.
//
// Phase 2 will replace this body with the libretro host initialisation.
// ─────────────────────────────────────────────────────────────────────────────

static bool g_initialised = false;
static const char* ENGINE_VERSION = "0.1.0-phase0";

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeInit(
        JNIEnv* env,
        jobject /* thiz */) {

    if (g_initialised) {
        LOGI("PocketEngine: already initialised");
        return env->NewStringUTF(ENGINE_VERSION);
    }

    LOGI("PocketEngine: initialising — version %s", ENGINE_VERSION);
    LOGI("PocketEngine: Phase 0 — JNI bridge verified");

    // TODO (Phase 2): initialise libretro host here

    g_initialised = true;
    LOGI("PocketEngine: init complete");

    return env->NewStringUTF(ENGINE_VERSION);
}

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeShutdown(
        JNIEnv* /* env */,
        jobject /* thiz */) {

    if (!g_initialised) return;

    LOGI("PocketEngine: shutting down");

    // TODO (Phase 2): unload libretro core here

    g_initialised = false;
    LOGI("PocketEngine: shutdown complete");
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetStatus(
        JNIEnv* env,
        jobject /* thiz */) {

    const char* status = g_initialised ? "READY" : "NOT_INIT";
    LOGI("PocketEngine: status query → %s", status);
    return env->NewStringUTF(status);
}
