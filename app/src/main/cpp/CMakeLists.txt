cmake_minimum_required(VERSION 3.22.1)

project("pocketlauncher")

# ─────────────────────────────────────────────────────────────────────────────
# Phase 0 — native library
#
# Builds the JNI bridge that proves Kotlin ↔ C++ works on both target devices.
# Phase 2 will add libretro.h and the core loader here.
# ─────────────────────────────────────────────────────────────────────────────

add_library(
    pocket-engine        # Library name — must match System.loadLibrary() in Kotlin
    SHARED               # Shared (.so) required by Android
    pocket_engine.cpp
)

# Android NDK log library — used for __android_log_print in pocket_engine.cpp
find_library(log-lib log)

target_link_libraries(
    pocket-engine
    ${log-lib}
)

# Enforce C++17 throughout
target_compile_features(pocket-engine PRIVATE cxx_std_17)
