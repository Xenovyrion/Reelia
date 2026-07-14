package com.reelia.app.ui.home

import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.model.UpcomingMovieItem
import com.reelia.app.ui.common.model.UpcomingShowItem

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
    val upcomingShows: List<UpcomingShowItem> = emptyList(),
    val upcomingMovies: List<UpcomingMovieItem> = emptyList(),
    val trending: List<HomeDiscoverItem> = emptyList(),
    val recentMovies: List<HomeDiscoverItem> = emptyList(),
    val recentShows: List<HomeDiscoverItem> = emptyList(),
    val suggestions: List<HomeDiscoverItem> = emptyList(),
)
