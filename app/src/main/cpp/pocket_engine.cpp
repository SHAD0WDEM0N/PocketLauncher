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


JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadGame(
        JNIEnv* env,
        jobject /* thiz */,
        jstring romPath) {
    if (!romPath) return JNI_FALSE;

    const char* chars = env->GetStringUTFChars(romPath, nullptr);
    const std::string path(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(romPath, chars);

    return g_core_loader.loadGame(path) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeUnloadGame(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    g_core_loader.unloadGame();
}

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeRunFrame(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return g_core_loader.runFrame() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameWidth(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return static_cast<jint>(g_core_loader.frameWidth());
}

JNIEXPORT jint JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameHeight(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return static_cast<jint>(g_core_loader.frameHeight());
}

JNIEXPORT jlong JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameCount(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return static_cast<jlong>(g_core_loader.frameCount());
}

JNIEXPORT jintArray JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeCopyFrameRgba(
        JNIEnv* env,
        jobject /* thiz */) {
    const auto frame = g_core_loader.copyFrameRgba();
    if (frame.empty()) return env->NewIntArray(0);

    jintArray result = env->NewIntArray(static_cast<jsize>(frame.size()));
    if (!result) return nullptr;

    env->SetIntArrayRegion(
        result,
        0,
        static_cast<jsize>(frame.size()),
        reinterpret_cast<const jint*>(frame.data())
    );
    return result;
}


JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeSetInputMask(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jint mask) {
    g_core_loader.setInputMask(static_cast<uint32_t>(mask));
}

JNIEXPORT jdouble JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetAudioSampleRate(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return static_cast<jdouble>(g_core_loader.audioSampleRate());
}

JNIEXPORT jshortArray JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeDrainAudio(
        JNIEnv* env,
        jobject /* thiz */) {
    const auto audio = g_core_loader.drainAudio();
    if (audio.empty()) return env->NewShortArray(0);

    jshortArray result = env->NewShortArray(static_cast<jsize>(audio.size()));
    if (!result) return nullptr;

    env->SetShortArrayRegion(
        result,
        0,
        static_cast<jsize>(audio.size()),
        reinterpret_cast<const jshort*>(audio.data())
    );
    return result;
}


JNIEXPORT jdouble JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetVideoFps(
        JNIEnv* /* env */,
        jobject /* thiz */) {
    return static_cast<jdouble>(g_core_loader.videoFps());
}


JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadSaveRam(
        JNIEnv* env,
        jobject /* thiz */,
        jstring path) {
    if (!path) return JNI_FALSE;
    const char* chars = env->GetStringUTFChars(path, nullptr);
    const std::string savePath(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(path, chars);
    return g_core_loader.loadSaveRam(savePath) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeSaveSaveRam(
        JNIEnv* env,
        jobject /* thiz */,
        jstring path) {
    if (!path) return JNI_FALSE;
    const char* chars = env->GetStringUTFChars(path, nullptr);
    const std::string savePath(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(path, chars);
    return g_core_loader.saveSaveRam(savePath) ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeSaveState(
        JNIEnv* env,
        jobject /* thiz */,
        jstring path) {
    if (!path) return JNI_FALSE;
    const char* chars = env->GetStringUTFChars(path, nullptr);
    const std::string statePath(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(path, chars);
    return g_core_loader.saveState(statePath) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadState(
        JNIEnv* env,
        jobject /* thiz */,
        jstring path) {
    if (!path) return JNI_FALSE;
    const char* chars = env->GetStringUTFChars(path, nullptr);
    const std::string statePath(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(path, chars);
    return g_core_loader.loadState(statePath) ? JNI_TRUE : JNI_FALSE;
}
