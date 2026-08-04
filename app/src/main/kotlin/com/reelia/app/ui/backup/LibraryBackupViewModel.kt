package com.reelia.app.ui.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.backup.LibraryBackupRepository
import com.reelia.app.data.backup.UnsupportedBackupVersionException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class LibraryBackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: LibraryBackupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryBackupUiState())
    val uiState: StateFlow<LibraryBackupUiState> = _uiState.asStateFlow()

    /** Called with the destination the user picked via the system's "create document" dialog —
     * null if they backed out of it, in which case there's nothing to do. */
    fun onExportTargetSelected(uri: Uri?) {
        if (uri == null) return
        _uiState.update { it.copy(isExporting = true, exportMessage = null) }
        viewModelScope.launch {
            val result = runCatching {
                val content = backupRepository.export()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output -> output.write(content.toByteArray()) }
                        ?: error("Couldn't open the destination file")
                }
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportMessage = if (result.isSuccess) {
                        ExportMessage.Success
                    } else {
                        ExportMessage.Failure(result.exceptionOrNull()?.message)
                    },
                )
            }
        }
    }

    fun onExportMessageShown() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    fun onFileSelected(uri: Uri) {
        _uiState.update { it.copy(importPhase = ImportPhase.Parsing) }
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                }.getOrNull()
            }
            if (content == null) {
                _uiState.update { it.copy(importPhase = ImportPhase.ParseFailed(isUnsupportedVersion = false)) }
                return@launch
            }
            runCatching { backupRepository.parse(content) }.fold(
                onSuccess = { parsed -> _uiState.update { it.copy(importPhase = ImportPhase.ReadyToImport(parsed)) } },
                onFailure = { e ->
                    val isUnsupportedVersion = e is UnsupportedBackupVersionException
                    _uiState.update { it.copy(importPhase = ImportPhase.ParseFailed(isUnsupportedVersion)) }
                },
            )
        }
    }

    fun startImport() {
        val ready = _uiState.value.importPhase as? ImportPhase.ReadyToImport ?: return
        _uiState.update { it.copy(importPhase = ImportPhase.Importing) }
        viewModelScope.launch {
            try {
                val report = backupRepository.import(ready.backup)
                _uiState.update { it.copy(importPhase = ImportPhase.Done(report)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(importPhase = ImportPhase.ImportFailed(e.message)) }
            }
        }
    }

    fun resetImport() {
        _uiState.update { it.copy(importPhase = ImportPhase.PickFile) }
    }
}
