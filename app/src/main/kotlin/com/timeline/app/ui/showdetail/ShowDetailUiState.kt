package com.timeline.app.ui.showdetail

import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.CastRowItem
import com.timeline.app.ui.common.components.WatchProviderRowItem

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

data class NextEpisodeUi(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val stillUrl: String?,
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
    val nextUnwatchedEpisode: NextEpisodeUi? = null,
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val trailerYoutubeKey: String? = null,
    val cast: List<CastRowItem> = emptyList(),
)
