package com.timeline.app.ui.home

import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.WatchStatus

data class ContinueWatchingItem(
    val showId: Int,
    val title: String,
    val backdropUrl: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String,
    val progress: Float,
    val status: WatchStatus,
)

data class HomeDiscoverItem(
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val year: String?,
)

enum class GreetingPeriod { MORNING, AFTERNOON, EVENING }

data class HomeUiState(
    val isLoading: Boolean = true,
    val greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING,
    val userFirstName: String? = null,
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val trending: List<HomeDiscoverItem> = emptyList(),
    val recentMovies: List<HomeDiscoverItem> = emptyList(),
    val recentShows: List<HomeDiscoverItem> = emptyList(),
    val suggestions: List<HomeDiscoverItem> = emptyList(),
)
