package com.timeline.app.ui.guide

import com.timeline.app.data.guide.GuideContent

sealed interface GuideUiState {
    data object Loading : GuideUiState
    data class Loaded(val content: GuideContent) : GuideUiState
    data class Error(val message: String?) : GuideUiState
}
