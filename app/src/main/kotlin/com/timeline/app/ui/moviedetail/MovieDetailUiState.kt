package com.timeline.app.ui.moviedetail

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val releaseDate: String? = null,
    val runtimeMinutes: Int? = null,
    val watched: Boolean = false,
)
