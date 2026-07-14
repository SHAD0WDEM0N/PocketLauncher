package com.pocketlauncher.utils

object RetroArchCoreMapping {
    data class CoreInfo(val name: String, val fileName: String)

    val systemToCores = mapOf(
        "Game Boy Advance" to listOf(
            CoreInfo("mGBA", "mgba_libretro_android.so"),
            CoreInfo("VBA-M", "vbam_libretro_android.so"),
            CoreInfo("gpSP", "gpsp_libretro_android.so")
        ),
        "Game Boy" to listOf(
            CoreInfo("Gambatte", "gambatte_libretro_android.so"),
            CoreInfo("Gearboy", "gearboy_libretro_android.so")
        ),
        "Game Boy Color" to listOf(
            CoreInfo("Gambatte", "gambatte_libretro_android.so"),
            CoreInfo("Gearboy", "gearboy_libretro_android.so")
        ),
        "NES" to listOf(
            CoreInfo("Nestopia UE", "nestopia_libretro_android.so"),
            CoreInfo("FCEUmm", "fceumm_libretro_android.so"),
            CoreInfo("Mesen", "mesen_libretro_android.so")
        ),
        "SNES" to listOf(
            CoreInfo("Snes9x", "snes9x_libretro_android.so"),
            CoreInfo("bsnes", "bsnes_libretro_android.so"),
            CoreInfo("Snes9x Current", "snes9x_libretro_android.so")
        ),
        "Nintendo 64" to listOf(
            CoreInfo("Mupen64Plus-Next", "mupen64plus_next_libretro_android.so")
        ),
        "Nintendo DS" to listOf(
            CoreInfo("melonDS", "melonds_libretro_android.so"),
            CoreInfo("DeSmuME", "desmume_libretro_android.so")
        ),
        "PS1" to listOf(
            CoreInfo("Beetle PSX HW", "mednafen_psx_hw_libretro_android.so"),
            CoreInfo("PCSX ReARMed", "pcsx_rearmed_libretro_android.so"),
            CoreInfo("SwanStation", "swanstation_libretro_android.so")
        ),
        "PSP" to listOf(
            CoreInfo("PPSSPP", "ppsspp_libretro_android.so")
        ),
        "Sega Genesis" to listOf(
            CoreInfo("Genesis Plus GX", "genesis_plus_gx_libretro_android.so"),
            CoreInfo("PicoDrive", "picodrive_libretro_android.so")
        ),
        "Dreamcast" to listOf(
            CoreInfo("Flycast", "flycast_libretro_android.so")
        ),
        "Arcade" to listOf(
            CoreInfo("FinalBurn Neo", "fbneo_libretro_android.so"),
            CoreInfo("MAME", "mame_libretro_android.so")
        )
    )
}
