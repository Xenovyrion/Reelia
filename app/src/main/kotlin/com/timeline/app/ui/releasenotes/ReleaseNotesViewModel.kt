package com.timeline.app.ui.releasenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.releasenotes.ReleaseNotesRepository
import com.timeline.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ReleaseNotesViewModel @Inject constructor(
    private val releaseNotesRepository: ReleaseNotesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReleaseNotesUiState>(ReleaseNotesUiState.Loading)
    val uiState: StateFlow<ReleaseNotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val language = settingsRepository.language.first()
            releaseNotesRepository.fetchReleaseNotes(language)
                .onSuccess { markdown -> _uiState.value = ReleaseNotesUiState.Loaded(markdown) }
                .onFailure { e -> _uiState.value = ReleaseNotesUiState.Error(e.message) }
        }
    }
}
