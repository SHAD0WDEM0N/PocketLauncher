package com.pocketlauncher.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector
import com.pocketlauncher.R

object RetroUtils {
    fun getSystemLogo(systemName: String): Int {
        return R.drawable.ic_logo // Generic placeholder
    }

    fun getSystemIcon(systemName: String): ImageVector {
        return when (systemName.lowercase()) {
            "nes", "snes", "nintendo 64", "n64" -> Icons.Default.Tv
            "game boy", "game boy color", "game boy advance", "psp", "nintendo ds", "nintendo 3ds" -> Icons.Default.StayCurrentPortrait
            "playstation", "ps1", "playstation 2", "ps2", "dreamcast", "gamecube", "wii" -> Icons.Default.Gamepad
            "arcade" -> Icons.Default.VideogameAsset
            else -> Icons.Default.VideogameAsset
        }
    }

    fun getRecommendedCores(systemName: String): List<RetroArchCoreMapping.CoreInfo> {
        return RetroArchCoreMapping.systemToCores[systemName] ?: emptyList()
    }

    val recommendedStandalone = mapOf(
        "PSP" to "org.ppsspp.ppsspp",
        "PS1" to "com.duckstation.android",
        "PlayStation 2" to "com.pcsx2.pcsx2",
        "NetherSX2" to "com.nether.pcsx2", // AetherSX2/NetherSX2
        "GameCube" to "org.dolphinemu.dolphinemu",
        "Wii" to "org.dolphinemu.dolphinemu",
        "Nintendo DS" to "com.dsemu.drastic",
        "Nintendo 3DS" to "com.citra.citra_emu"
    )
}
