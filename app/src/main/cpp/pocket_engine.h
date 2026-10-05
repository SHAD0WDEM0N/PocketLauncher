#ifndef POCKET_ENGINE_H
#define POCKET_ENGINE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeInit(
        JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeShutdown(
        JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetStatus(
        JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadCore(
        JNIEnv* env, jobject thiz, jstring path);

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeUnloadCore(
        JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreName(
        JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreVersion(
        JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreExtensions(
        JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetCoreError(
        JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeLoadGame(
        JNIEnv* env, jobject thiz, jstring romPath);

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeUnloadGame(
        JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeRunFrame(
        JNIEnv* env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameWidth(
        JNIEnv* env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameHeight(
        JNIEnv* env, jobject thiz);

JNIEXPORT jlong JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetFrameCount(
        JNIEnv* env, jobject thiz);

JNIEXPORT jintArray JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeCopyFrameRgba(
        JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeSetInputMask(
        JNIEnv* env, jobject thiz, jint mask);

JNIEXPORT jdouble JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeGetAudioSampleRate(
        JNIEnv* env, jobject thiz);

JNIEXPORT jshortArray JNICALL
Java_com_example_pocketlauncher_engine_PocketEngine_nativeDrainAudio(
        JNIEnv* env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif
