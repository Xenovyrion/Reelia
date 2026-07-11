package com.timeline.app.ui.moviedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.domain.usecase.MarkMovieWatchedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val markMovieWatchedUseCase: MarkMovieWatchedUseCase,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    val uiState: StateFlow<MovieDetailUiState> = movieRepository.getMovie(movieId)
        .filterNotNull()
        .map { movie ->
            MovieDetailUiState(
                isLoading = false,
                title = movie.title,
                overview = movie.overview,
                posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                heroUrl = imageUrlBuilder.posterUrl(movie.posterPath, size = "w780"),
                releaseDate = movie.releaseDate,
                runtimeMinutes = movie.runtimeMinutes,
                watched = movie.watched,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MovieDetailUiState(),
        )

    fun onWatchedToggled(watched: Boolean) {
        viewModelScope.launch {
            markMovieWatchedUseCase(movieId, watched)
        }
    }
}
