cmake_minimum_required(VERSION 3.22.1)

project("pocketlauncher")

add_library(
    pocket-engine
    SHARED
    pocket_engine.cpp
    core_loader.cpp
)

find_library(log-lib log)
find_library(dl-lib dl)

target_link_libraries(
    pocket-engine
    ${log-lib}
    ${dl-lib}
)

target_compile_features(pocket-engine PRIVATE cxx_std_17)
