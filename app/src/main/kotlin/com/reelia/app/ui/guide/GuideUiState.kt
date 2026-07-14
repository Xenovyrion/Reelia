package com.reelia.app.ui.guide

import com.reelia.app.data.guide.GuideContent

sealed interface GuideUiState {
    data object Loading : GuideUiState
    data class Loaded(val content: GuideContent) : GuideUiState
    data class Error(val message: String?) : GuideUiState
}
