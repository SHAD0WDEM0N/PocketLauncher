package com.pocketlauncher.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketlauncher.data.RomScanner
import com.pocketlauncher.data.local.RomEntity
import com.pocketlauncher.data.local.RomFolderAssignment
import com.pocketlauncher.repository.RomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RetroSystem(
    val name: String,
    val romCount: Int,
    val assignedEmulator: String? = null
)

class RomViewModel(
    private val romRepository: RomRepository,
    private val romScanner: RomScanner
) : ViewModel() {

    val roms: StateFlow<List<RomEntity>> = romRepository.getAllRoms()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val folderAssignments: StateFlow<List<RomFolderAssignment>> = romRepository.getAllFolderAssignments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val systems: StateFlow<List<RetroSystem>> = roms.map { romList ->
        romList.groupBy { it.systemName }
            .map { (name, list) -> RetroSystem(name, list.size) }
            .sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun addFolderAssignment(assignment: RomFolderAssignment) {
        viewModelScope.launch {
            romRepository.addFolderAssignment(assignment)
            scanLibrary()
        }
    }

    fun deleteFolderAssignment(assignment: RomFolderAssignment) {
        viewModelScope.launch {
            romRepository.deleteFolderAssignment(assignment)
        }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            withContext(Dispatchers.IO) {
                val assignments = folderAssignments.value
                val allFoundRoms = mutableListOf<RomEntity>()
                assignments.forEach { assignment ->
                    val found = romScanner.scanFolderWithAssignment(assignment)
                    allFoundRoms.addAll(found)
                }
                romRepository.saveRoms(allFoundRoms)
            }
            _isScanning.value = false
        }
    }

    fun detectSystem(folderName: String): String? {
        return romScanner.detectSystemFromFolderName(folderName)
    }

    fun clearLibrary() {
        viewModelScope.launch {
            romRepository.clearRoms()
        }
    }
}
