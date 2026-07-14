package com.reelia.app.ui.releasenotes

import com.reelia.app.data.releasenotes.ReleaseNoteVersion

sealed interface ReleaseNotesUiState {
    data object Loading : ReleaseNotesUiState
    data class Loaded(val versions: List<ReleaseNoteVersion>) : ReleaseNotesUiState
    data class Error(val message: String?) : ReleaseNotesUiState
}
