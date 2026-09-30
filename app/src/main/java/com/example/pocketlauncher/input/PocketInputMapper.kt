package com.example.pocketlauncher.input

import android.view.KeyEvent

/**
 * PocketInputMapper
 *
 * Translates raw Android [KeyEvent.keyCode] values into [PocketButton]
 * entries. A null return means the key is not mapped and should be ignored.
 *
 * Device profiles are layered: device-specific overrides first, then the
 * generic Android gamepad standard. Adding support for a new handheld
 * only requires adding a new entry to [deviceProfiles].
 */
object PocketInputMapper {

    // -----------------------------------------------------------------------
    // Generic gamepad layout — covers standard Android gamepad key codes
    // (BUTTON_A, DPAD_UP, etc.)  which most handhelds expose by default.
    // -----------------------------------------------------------------------
    private val genericProfile: Map<Int, PocketButton> = mapOf(
        KeyEvent.KEYCODE_BUTTON_A     to PocketButton.A,
        KeyEvent.KEYCODE_BUTTON_B     to PocketButton.B,
        KeyEvent.KEYCODE_BUTTON_X     to PocketButton.X,
        KeyEvent.KEYCODE_BUTTON_Y     to PocketButton.Y,
        KeyEvent.KEYCODE_BUTTON_L1    to PocketButton.L1,
        KeyEvent.KEYCODE_BUTTON_R1    to PocketButton.R1,
        KeyEvent.KEYCODE_BUTTON_L2    to PocketButton.L2,
        KeyEvent.KEYCODE_BUTTON_R2    to PocketButton.R2,
        KeyEvent.KEYCODE_BUTTON_START to PocketButton.START,
        KeyEvent.KEYCODE_BUTTON_SELECT to PocketButton.SELECT,
        KeyEvent.KEYCODE_DPAD_UP      to PocketButton.UP,
        KeyEvent.KEYCODE_DPAD_DOWN    to PocketButton.DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT    to PocketButton.LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT   to PocketButton.RIGHT,
        KeyEvent.KEYCODE_MENU         to PocketButton.MENU,
    )

    // -----------------------------------------------------------------------
    // Device-specific overrides
    // Key: android.os.Build.MODEL substring to match
    // Value: partial map that supplements / overrides the generic profile
    // -----------------------------------------------------------------------
    private val deviceProfiles: Map<String, Map<Int, PocketButton>> = mapOf(
        // Retroid Pocket G2 — uses standard Android gamepad codes, no overrides needed.
        "Retroid Pocket G2" to emptyMap(),

        // Mangmi Pocket Air Y — uses standard codes; expand here if issues arise.
        "Pocket Air Y" to emptyMap(),
    )

    /**
     * Resolves the model-specific override map for the current device.
     */
    private val activeProfile: Map<Int, PocketButton> by lazy {
        val model = android.os.Build.MODEL
        val override = deviceProfiles.entries
            .firstOrNull { (key, _) -> model.contains(key, ignoreCase = true) }
            ?.value ?: emptyMap()
        // Overrides take priority over generic profile
        genericProfile + override
    }

    /**
     * Map a raw [KeyEvent] to a [PocketButton], or null if unmapped.
     */
    fun map(event: KeyEvent): PocketButton? = activeProfile[event.keyCode]

    /**
     * Convenience overload for a raw keyCode integer.
     */
    fun map(keyCode: Int): PocketButton? = activeProfile[keyCode]
}
