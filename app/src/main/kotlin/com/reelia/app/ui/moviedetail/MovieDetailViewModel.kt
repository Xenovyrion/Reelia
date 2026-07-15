package com.reelia.app.ui.moviedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.metadata.MetadataProviderRegistry
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.domain.model.WatchProviderOption
import com.reelia.app.domain.usecase.MarkMovieWatchedUseCase
import com.reelia.app.ui.common.components.CastRowItem
import com.reelia.app.ui.common.components.WatchProviderRowItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class MovieDetailExtras(
    val cast: List<CastRowItem> = emptyList(),
    val crew: List<CastRowItem> = emptyList(),
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val trailerYoutubeKey: String? = null,
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val markMovieWatchedUseCase: MarkMovieWatchedUseCase,
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val extras = MutableStateFlow(MovieDetailExtras())

    private val removedEventChannel = Channel<Unit>(Channel.BUFFERED)
    val removedEvent: Flow<Unit> = removedEventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val preview = provider.getMoviePreview(movieId)
                extras.value = MovieDetailExtras(
                    cast = preview.cast.map {
                        CastRowItem(
                            personId = it.id,
                            name = it.name,
                            character = it.character,
                            photoUrl = imageUrlBuilder.posterUrl(it.profilePath, size = "w185"),
                        )
                    },
                    crew = preview.crew.map {
                        CastRowItem(
                            personId = it.id,
                            name = it.name,
                            character = it.job,
                            photoUrl = imageUrlBuilder.posterUrl(it.profilePath, size = "w185"),
                        )
                    },
                    watchProvidersFlatrate = preview.watchProviders?.flatrate.orEmpty().toRowItems(),
                    watchProvidersRent = preview.watchProviders?.rent.orEmpty().toRowItems(),
                    watchProvidersBuy = preview.watchProviders?.buy.orEmpty().toRowItems(),
                    trailerYoutubeKey = preview.trailerYoutubeKey,
                )
            } catch (e: Exception) {
                // Live enrichment only — a network failure here must never block the
                // Room-backed core UI, so the extras just stay at their empty defaults.
            }
        }
    }

    val uiState: StateFlow<MovieDetailUiState> = combine(
        movieRepository.getMovie(movieId).filterNotNull(),
        movieRepository.getGenresForMovie(movieId),
        extras,
    ) { movie, genres, extra ->
        MovieDetailUiState(
            isLoading = false,
            title = movie.title,
            overview = movie.overview,
            posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
            heroUrl = imageUrlBuilder.posterUrl(movie.posterPath, size = "w780"),
            releaseDate = movie.releaseDate,
            runtimeMinutes = movie.runtimeMinutes,
            watched = movie.watched,
            voteAverage = movie.userRating,
            genreNames = genres.map { it.name },
            cast = extra.cast,
            crew = extra.crew,
            watchProvidersFlatrate = extra.watchProvidersFlatrate,
            watchProvidersRent = extra.watchProvidersRent,
            watchProvidersBuy = extra.watchProvidersBuy,
            trailerYoutubeKey = extra.trailerYoutubeKey,
            isFavorite = movie.isFavorite,
            contentRating = movie.contentRating,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovieDetailUiState(),
    )

    fun onWatchedToggled(watched: Boolean) {
        viewModelScope.launch {
            markMovieWatchedUseCase(movieId, watched)
        }
    }

    fun onFavoriteToggled(isFavorite: Boolean) {
        viewModelScope.launch {
            movieRepository.setFavorite(movieId, isFavorite)
        }
    }

    fun onRemoveConfirmed() {
        viewModelScope.launch {
            movieRepository.removeMovie(movieId)
            removedEventChannel.send(Unit)
        }
    }

    private suspend fun List<WatchProviderOption>.toRowItems(): List<WatchProviderRowItem> =
        map { WatchProviderRowItem(name = it.providerName, logoUrl = imageUrlBuilder.posterUrl(it.logoPath, size = "w92")) }
}
