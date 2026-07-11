package com.timeline.app.ui.addmedia

import com.timeline.app.domain.model.TmdbSearchResult

data class AddMediaResultUi(
    val result: TmdbSearchResult,
    val posterUrl: String?,
)

data class AddMediaUiState(
    val query: String = "",
    val results: List<AddMediaResultUi> = emptyList(),
    val isSearching: Boolean = false,
    val isAdding: Boolean = false,
    val hasApiKey: Boolean = true,
    val errorMessage: String? = null,
)
