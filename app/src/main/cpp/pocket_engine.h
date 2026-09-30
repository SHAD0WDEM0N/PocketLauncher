#ifndef POCKET_ENGINE_H
#define POCKET_ENGINE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * PocketEngine — Phase 0 JNI bridge.
 *
 * In Phase 0 this simply proves that Kotlin → JNI → C++ works on both
 * target devices. Later phases add the libretro host here.
 */

/**
 * Called from Kotlin when the app starts.
 * Returns the native engine version string.
 */
JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeInit(
        JNIEnv* env,
        jobject thiz);

/**
 * Called from Kotlin on app shutdown.
 * Cleans up native resources.
 */
JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeShutdown(
        JNIEnv* env,
        jobject thiz);

/**
 * Returns a status string for the engine status badge.
 * Phase 0: always "READY". Later phases: core load state, etc.
 */
JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetStatus(
        JNIEnv* env,
        jobject thiz);

#ifdef __cplusplus
}
#endif

#endif // POCKET_ENGINE_H
