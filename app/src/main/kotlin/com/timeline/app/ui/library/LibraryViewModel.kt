package com.timeline.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.WatchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class LibraryViewModel @Inject constructor(
    showRepository: ShowRepository,
    movieRepository: MovieRepository,
    imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = combine(
        showRepository.getAllShows(),
        movieRepository.getAllMovies(),
        showRepository.getEpisodeProgressByShow(),
    ) { shows, movies, progressByShow ->
        val progressById = progressByShow.associateBy { it.showId }
        val showItems = shows.map { show ->
            val progress = progressById[show.tmdbId]
            LibraryItem(
                id = show.tmdbId,
                mediaType = MediaType.TV,
                title = show.name,
                posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                progress = progress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total },
                isCompleted = show.status == WatchStatus.COMPLETED,
            )
        }
        val movieItems = movies.map { movie ->
            LibraryItem(
                id = movie.tmdbId,
                mediaType = MediaType.MOVIE,
                title = movie.title,
                posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                progress = if (movie.watched) 1f else null,
                isCompleted = movie.watched,
            )
        }
        LibraryUiState(isLoading = false, items = showItems + movieItems)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )
}
