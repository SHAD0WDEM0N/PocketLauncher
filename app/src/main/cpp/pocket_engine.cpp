#include "pocket_engine.h"
#include "core_loader.h"

#include <android/log.h>
#include <string>

#define LOG_TAG "PocketEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool g_initialised = false;
static const char* ENGINE_VERSION = "0.3.0-libretro";
static CoreLoader g_core_loader;

static jstring toJString(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeInit(
        JNIEnv* env,
        jobject /* thiz */) {
    if (!g_initialised) {
        LOGI("PocketEngine initialising — version %s", ENGINE_VERSION);
        g_initialised = true;
    }
    return env->NewStringUTF(ENGINE_VERSION);
}

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeShutdown(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    if (!g_initialised) return;

    g_core_loader.unload();
    g_initialised = false;
    LOGI("PocketEngine shutdown complete");
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetStatus(
        JNIEnv* env,
        jobject /* thiz */) {
    return env->NewStringUTF(g_initialised ? "READY" : "NOT_INIT");
}

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadCore(
        JNIEnv* env,
        jobject /* thiz */,
        jstring path) {
    if (!path) return JNI_FALSE;

    const char* chars = env->GetStringUTFChars(path, nullptr);
    const std::string core_path(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(path, chars);

    return g_core_loader.load(core_path) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeUnloadCore(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    g_core_loader.unload();
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreName(
        JNIEnv* env,
        jobject /* thiz */) {
    return toJString(env, g_core_loader.libraryName());
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreVersion(
        JNIEnv* env,
        jobject /* thiz */) {
    return toJString(env, g_core_loader.libraryVersion());
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreExtensions(
        JNIEnv* env,
        jobject /* thiz */) {
    return toJString(env, g_core_loader.validExtensions());
}

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreError(
        JNIEnv* env,
        jobject /* thiz */) {
    return toJString(env, g_core_loader.lastError());
}
