package com.reelia.app.ui.showdetail

import com.reelia.app.domain.model.ShowBroadcastStatus
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.components.CastRowItem
import com.reelia.app.ui.common.components.WatchProviderRowItem

data class EpisodeUi(
    val episodeNumber: Int,
    val name: String,
    val watched: Boolean,
    val overview: String? = null,
    val voteAverage: Float? = null,
    val stillUrl: String? = null,
    val airDate: String? = null,
)

data class SeasonUi(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val episodes: List<EpisodeUi>,
    val airDate: String? = null,
)

data class NextEpisodeUi(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val stillUrl: String?,
    val airDate: String? = null,
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
    val crew: List<CastRowItem> = emptyList(),
    val broadcastStatus: ShowBroadcastStatus = ShowBroadcastStatus.UNKNOWN,
    val networkNames: String? = null,
    val yearRange: String? = null,
    val genreNames: List<String> = emptyList(),
    val nextEpisodeAirDate: String? = null,
    val averageEpisodeRuntimeMinutes: Int? = null,
    val isFavorite: Boolean = false,
    val contentRating: String? = null,
)
