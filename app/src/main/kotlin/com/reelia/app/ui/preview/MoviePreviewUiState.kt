package com.reelia.app.ui.preview

import androidx.annotation.StringRes
import com.reelia.app.ui.common.components.CastRowItem
import com.reelia.app.ui.common.components.WatchProviderRowItem

data class MoviePreviewUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val releaseDate: String? = null,
    val runtimeMinutes: Int? = null,
    val voteAverage: Float? = null,
    val contentRating: String? = null,
    val genreNames: List<String> = emptyList(),
    val cast: List<CastRowItem> = emptyList(),
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val isAdding: Boolean = false,
    val isInLibrary: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)
