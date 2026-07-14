package com.reelia.app.ui.preview

import androidx.annotation.StringRes
import com.reelia.app.ui.common.components.CastRowItem
import com.reelia.app.ui.common.components.WatchProviderRowItem

data class ShowPreviewUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val firstAirDate: String? = null,
    val numberOfSeasons: Int? = null,
    val voteAverage: Float? = null,
    val genreNames: List<String> = emptyList(),
    val networkNames: List<String> = emptyList(),
    val cast: List<CastRowItem> = emptyList(),
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val isAdding: Boolean = false,
    val added: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)
