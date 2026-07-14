package com.pocketlauncher.data

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.pocketlauncher.data.local.RomEntity
import com.pocketlauncher.data.local.RomFolderAssignment
import com.pocketlauncher.data.models.RomSystem

class RomScanner(private val context: Context) {

    private val supportedSystems = listOf(
        RomSystem("NES", "com.retroarch", listOf("nes")),
        RomSystem("SNES", "com.retroarch", listOf("smc", "sfc", "zip")),
        RomSystem("N64", "com.retroarch", listOf("n64", "z64", "v64")),
        RomSystem("Game Boy", "com.retroarch", listOf("gb")),
        RomSystem("Game Boy Color", "com.retroarch", listOf("gbc")),
        RomSystem("Game Boy Advance", "com.retroarch", listOf("gba")),
        RomSystem("Nintendo DS", "com.dsemu.drastic", listOf("nds")),
        RomSystem("PS1", "com.epsxe.ePSXe", listOf("iso", "bin", "cue", "chd", "pbp")),
        RomSystem("PSP", "org.ppsspp.ppsspp", listOf("iso", "cso")),
        RomSystem("Nintendo 3DS", "com.citra.citra_emu", listOf("3ds", "cia")),
        RomSystem("Sega Genesis", "com.retroarch", listOf("md", "gen", "smd")),
        RomSystem("Sega Master System", "com.retroarch", listOf("sms")),
        RomSystem("PlayStation 2", "com.pcsx2.pcsx2", listOf("iso", "chd")),
        RomSystem("Dreamcast", "com.reicast.emulator", listOf("cdi", "gdi", "chd")),
        RomSystem("GameCube", "org.dolphinemu.dolphinemu", listOf("iso", "gcm", "rvz")),
        RomSystem("Wii", "org.dolphinemu.dolphinemu", listOf("iso", "wbfs", "rvz")),
        RomSystem("Arcade", "com.retroarch", listOf("zip", "7z")),
        RomSystem("Xbox", "", listOf("iso")),
        RomSystem("Xbox 360", "", listOf("iso", "xex"))
    )

    private val systemAliases = mapOf(
        "psp" to "PSP",
        "ps1" to "PS1", "psx" to "PS1", "playstation" to "PS1",
        "ps2" to "PlayStation 2",
        "gba" to "Game Boy Advance", "gameboyadvance" to "Game Boy Advance",
        "gb" to "Game Boy", "gameboy" to "Game Boy",
        "gbc" to "Game Boy Color", "gameboycolor" to "Game Boy Color",
        "nes" to "NES",
        "snes" to "SNES", "supernintendo" to "SNES",
        "n64" to "Nintendo 64", "nintendo64" to "Nintendo 64",
        "nds" to "Nintendo DS", "ds" to "Nintendo DS",
        "3ds" to "Nintendo 3DS",
        "gc" to "GameCube", "gamecube" to "GameCube",
        "wii" to "Wii",
        "genesis" to "Sega Genesis", "megadrive" to "Sega Genesis",
        "sms" to "Sega Master System", "mastersystem" to "Sega Master System",
        "dc" to "Dreamcast", "dreamcast" to "Dreamcast",
        "xbox" to "Xbox",
        "xbox360" to "Xbox 360",
        "arcade" to "Arcade", "mame" to "Arcade"
    )

    fun detectSystemFromFolderName(folderName: String): String? {
        val normalized = folderName.lowercase().replace(" ", "")
        return systemAliases[normalized] ?: systemAliases.entries.find { normalized.contains(it.key) }?.value
    }

    fun scanFolderWithAssignment(assignment: RomFolderAssignment): List<RomEntity> {
        val root = DocumentFile.fromTreeUri(context, android.net.Uri.parse(assignment.folderUri)) ?: return emptyList()
        val foundRoms = mutableListOf<RomEntity>()
        scanRecursive(root, foundRoms, assignment.systemName, assignment.folderUri)
        return foundRoms
    }

    private fun scanRecursive(
        directory: DocumentFile,
        results: MutableList<RomEntity>,
        assignedSystem: String?,
        folderUri: String
    ) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanRecursive(file, results, assignedSystem, folderUri)
            } else {
                val extension = file.name?.substringAfterLast('.', "")?.lowercase() ?: ""
                
                val systemName = if (assignedSystem != null) {
                    // If we have an assigned system, we still check extension to make sure it's a "game" 
                    // (optional, but good for safety)
                    val isGameExtension = supportedSystems.any { it.supportedExtensions.contains(extension) }
                    if (isGameExtension) assignedSystem else null
                } else {
                    // Fallback to extension matching
                    supportedSystems.find { it.supportedExtensions.contains(extension) }?.name
                }
                
                if (systemName != null) {
                    results.add(
                        RomEntity(
                            filePath = file.uri.toString(),
                            title = file.name?.substringBeforeLast('.') ?: "Unknown",
                            systemName = systemName,
                            folderUri = folderUri
                        )
                    )
                }
            }
        }
    }
}
