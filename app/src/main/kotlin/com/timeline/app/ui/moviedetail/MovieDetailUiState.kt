package com.timeline.app.ui.moviedetail

import com.timeline.app.ui.common.components.CastRowItem
import com.timeline.app.ui.common.components.WatchProviderRowItem

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    // Poster fetched at a larger size, standing in for a true backdrop until TrackedMovieEntity
    // gets a real backdropPath field (movies don't have one today, unlike shows).
    val heroUrl: String? = null,
    val releaseDate: String? = null,
    val runtimeMinutes: Int? = null,
    val watched: Boolean = false,
    val voteAverage: Float? = null,
    val genreNames: List<String> = emptyList(),
    val cast: List<CastRowItem> = emptyList(),
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val trailerYoutubeKey: String? = null,
    val directorNames: String? = null,
)
