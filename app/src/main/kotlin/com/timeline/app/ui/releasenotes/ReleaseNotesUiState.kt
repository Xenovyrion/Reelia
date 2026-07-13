package com.timeline.app.ui.releasenotes

sealed interface ReleaseNotesUiState {
    data object Loading : ReleaseNotesUiState
    data class Loaded(val markdown: String) : ReleaseNotesUiState
    data class Error(val message: String?) : ReleaseNotesUiState
}
