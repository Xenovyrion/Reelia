package com.timeline.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.local.dao.ShowEpisodeProgress
import com.timeline.app.data.local.entity.EpisodeEntity
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.effectiveMovieStatus
import com.timeline.app.ui.common.effectiveShowStatus
import com.timeline.app.ui.library.LibraryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class RawHomeData(
    val shows: List<TrackedShowEntity>,
    val progress: List<ShowEpisodeProgress>,
    val unwatchedEpisodes: List<EpisodeEntity>,
    val movies: List<TrackedMovieEntity>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    init {
        // One-time backfill: status used to only ever be set once at add-time, so anything
        // already fully watched before this fix shipped is stuck showing "à voir" until
        // reconciled against real progress here.
        viewModelScope.launch {
            showRepository.reconcileAllStatuses()
            movieRepository.reconcileAllStatuses()
        }
    }

    private val rawData = combine(
        showRepository.getAllShows(),
        showRepository.getEpisodeProgressByShow(),
        showRepository.getAllUnwatchedEpisodesOrdered(),
        movieRepository.getAllMovies(),
    ) { shows, progress, unwatchedEpisodes, movies ->
        RawHomeData(shows, progress, unwatchedEpisodes, movies)
    }

    val uiState: StateFlow<HomeUiState> = rawData
        .map { raw ->
            val progressByShowId = raw.progress.associateBy { it.showId }
            val nextEpisodeByShowId = raw.unwatchedEpisodes.groupBy { it.showId }.mapValues { it.value.first() }

            val continueWatching = raw.shows
                .filter { effectiveShowStatus(it.status, progressByShowId[it.tmdbId]) == WatchStatus.WATCHING }
                .mapNotNull { show ->
                    val nextEpisode = nextEpisodeByShowId[show.tmdbId] ?: return@mapNotNull null
                    val showProgress = progressByShowId[show.tmdbId]
                    val progress = showProgress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total } ?: 0f
                    ContinueWatchingItem(
                        showId = show.tmdbId,
                        title = show.name,
                        backdropUrl = imageUrlBuilder.backdropUrl(show.backdropPath),
                        seasonNumber = nextEpisode.seasonNumber,
                        episodeNumber = nextEpisode.episodeNumber,
                        episodeName = nextEpisode.name,
                        progress = progress,
                        status = effectiveShowStatus(show.status, showProgress),
                    )
                }

            val showItems = raw.shows.map { show ->
                val showProgress = progressByShowId[show.tmdbId]
                LibraryItem(
                    id = show.tmdbId,
                    mediaType = MediaType.TV,
                    title = show.name,
                    posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                    progress = showProgress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total },
                    status = effectiveShowStatus(show.status, showProgress),
                    isFavorite = show.isFavorite,
                    addedAt = show.addedAt,
                )
            }
            val movieItems = raw.movies.map { movie ->
                LibraryItem(
                    id = movie.tmdbId,
                    mediaType = MediaType.MOVIE,
                    title = movie.title,
                    posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                    progress = if (movie.watched) 1f else null,
                    status = effectiveMovieStatus(movie.status, movie.watched),
                    isFavorite = movie.isFavorite,
                    addedAt = movie.addedAt,
                )
            }

            HomeUiState(
                isLoading = false,
                continueWatching = continueWatching,
                libraryItems = (showItems + movieItems).sortedByDescending { it.addedAt },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
