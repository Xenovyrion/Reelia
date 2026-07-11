package com.timeline.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
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
    ) { shows, movies ->
        val showItems = shows.map { show ->
            LibraryItem(
                id = show.tmdbId,
                mediaType = MediaType.TV,
                title = show.name,
                posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
            )
        }
        val movieItems = movies.map { movie ->
            LibraryItem(
                id = movie.tmdbId,
                mediaType = MediaType.MOVIE,
                title = movie.title,
                posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
            )
        }
        LibraryUiState(isLoading = false, items = showItems + movieItems)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )
}
