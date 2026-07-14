package com.reelia.app.ui.tvtimeimport

import com.reelia.app.data.tvtimeimport.TvTimeImportData
import com.reelia.app.data.tvtimeimport.TvTimeImportReport

sealed interface TvTimeImportUiState {
    data object PickFile : TvTimeImportUiState
    data object Parsing : TvTimeImportUiState
    data class ParseFailed(val message: String?) : TvTimeImportUiState
    data class ReadyToImport(val data: TvTimeImportData) : TvTimeImportUiState
    data class Importing(val done: Int, val total: Int) : TvTimeImportUiState
    data class ImportFailed(val message: String?) : TvTimeImportUiState
    data class Done(val report: TvTimeImportReport) : TvTimeImportUiState
}
