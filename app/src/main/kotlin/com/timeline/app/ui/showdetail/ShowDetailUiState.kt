package com.timeline.app.ui.showdetail

import com.timeline.app.domain.model.WatchStatus

data class EpisodeUi(
    val episodeNumber: Int,
    val name: String,
    val watched: Boolean,
    val overview: String? = null,
    val voteAverage: Float? = null,
    val stillUrl: String? = null,
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
    val backdropUrl: String? = null,
    val status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
    val userRating: Float? = null,
    val seasonCount: Int = 0,
    val watchedEpisodeCount: Int = 0,
    val totalEpisodeCount: Int = 0,
    val seasons: List<SeasonUi> = emptyList(),
)
