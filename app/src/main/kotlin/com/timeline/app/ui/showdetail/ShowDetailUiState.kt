package com.timeline.app.ui.showdetail

data class EpisodeUi(
    val episodeNumber: Int,
    val name: String,
    val watched: Boolean,
)

data class SeasonUi(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val episodes: List<EpisodeUi>,
)

data class ShowDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val seasons: List<SeasonUi> = emptyList(),
)
