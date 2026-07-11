package com.timeline.app.ui.preview

import com.timeline.app.ui.common.components.CastRowItem
import com.timeline.app.ui.common.components.WatchProviderRowItem

data class MoviePreviewUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val releaseDate: String? = null,
    val runtimeMinutes: Int? = null,
    val voteAverage: Float? = null,
    val genreNames: List<String> = emptyList(),
    val cast: List<CastRowItem> = emptyList(),
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val isAdding: Boolean = false,
    val added: Boolean = false,
    val errorMessage: String? = null,
)
