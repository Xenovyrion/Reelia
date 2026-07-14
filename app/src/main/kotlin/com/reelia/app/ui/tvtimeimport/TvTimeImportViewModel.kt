package com.reelia.app.ui.tvtimeimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.tvtimeimport.TvTimeExportParser
import com.reelia.app.data.tvtimeimport.TvTimeImportProgress
import com.reelia.app.data.tvtimeimport.TvTimeImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class TvTimeImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportParser: TvTimeExportParser,
    private val importRepository: TvTimeImportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvTimeImportUiState>(TvTimeImportUiState.PickFile)
    val uiState: StateFlow<TvTimeImportUiState> = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri) {
        _uiState.value = TvTimeImportUiState.Parsing
        viewModelScope.launch {
            val data = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { exportParser.parse(it) }
                }
            }.getOrNull()

            if (data == null || (data.shows.isEmpty() && data.movies.isEmpty())) {
                _uiState.value = TvTimeImportUiState.ParseFailed(null)
                return@launch
            }
            _uiState.value = TvTimeImportUiState.ReadyToImport(data)
        }
    }

    fun startImport() {
        val ready = _uiState.value as? TvTimeImportUiState.ReadyToImport ?: return
        val total = ready.data.shows.size + ready.data.movies.size
        _uiState.value = TvTimeImportUiState.Importing(0, total)
        viewModelScope.launch {
            try {
                val report = importRepository.import(ready.data) { progress: TvTimeImportProgress ->
                    _uiState.value = TvTimeImportUiState.Importing(progress.done, progress.total)
                }
                _uiState.value = TvTimeImportUiState.Done(report)
            } catch (e: Exception) {
                _uiState.value = TvTimeImportUiState.ImportFailed(e.message)
            }
        }
    }

    fun reset() {
        _uiState.value = TvTimeImportUiState.PickFile
    }
}
