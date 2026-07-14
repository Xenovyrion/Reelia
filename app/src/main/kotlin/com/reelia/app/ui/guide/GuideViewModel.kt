package com.reelia.app.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.guide.GuideParser
import com.reelia.app.data.guide.GuideRepository
import com.reelia.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val guideRepository: GuideRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuideUiState>(GuideUiState.Loading)
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val language = settingsRepository.language.first()
            guideRepository.fetchGuide(language)
                .onSuccess { markdown -> _uiState.value = GuideUiState.Loaded(GuideParser.parse(markdown)) }
                .onFailure { e -> _uiState.value = GuideUiState.Error(e.message) }
        }
    }
}
