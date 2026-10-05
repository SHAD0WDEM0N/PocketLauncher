package com.example.pocketlauncher.input

/**
 * PocketInput — hardware-agnostic controller abstraction.
 *
 * Every piece of UI and every future emulator binding talks to
 * PocketButton rather than raw Android KeyEvent codes. This means
 * we only need one device-mapping table to support Retroid, Mangmi,
 * Anbernic, AYANEO, or any other Android handheld.
 */
enum class PocketButton {
    UP, DOWN, LEFT, RIGHT,
    A, B, X, Y,
    L1, R1, L2, R2,
    L3, R3,
    START, SELECT,
    MENU,           // Home / launcher button
}
