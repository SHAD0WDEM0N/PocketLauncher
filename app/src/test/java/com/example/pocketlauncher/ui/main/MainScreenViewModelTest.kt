package com.example.pocketlauncher.ui.main

import com.example.pocketlauncher.library.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun supportedPlatforms_haveExpectedDisplayNames() {
        assertEquals("Game Boy", Platform.GB.displayName)
        assertEquals("Game Boy Color", Platform.GBC.displayName)
        assertEquals("Game Boy Advance", Platform.GBA.displayName)
    }

    @Test
    fun supportedPlatforms_recogniseExpectedRomExtensions() {
        assertTrue("gb" in Platform.GB.extensions)
        assertTrue("gbc" in Platform.GBC.extensions)
        assertTrue("gba" in Platform.GBA.extensions)
    }
}
