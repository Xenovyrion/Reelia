package com.timeline.app.ui.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.GenreOption
import com.timeline.app.ui.common.components.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class RawFilmsData(
    val movies: List<TrackedMovieEntity>,
    val genres: List<GenreEntity>,
    val crossRefs: List<MovieGenreCrossRef>,
)

private data class FilterState(
    val viewMode: ViewMode = ViewMode.GRID,
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)

@HiltViewModel
class FilmsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())

    private val rawData = combine(
        movieRepository.getAllMovies(),
        movieRepository.getGenresForTrackedMovies(),
        movieRepository.getMovieGenreCrossRefs(),
    ) { movies, genres, crossRefs -> RawFilmsData(movies, genres, crossRefs) }

    val uiState: StateFlow<FilmsUiState> = combine(rawData, filterState) { raw, filter ->
        val genreIdsByMovieId = raw.crossRefs.groupBy({ it.movieId }, { it.genreId })

        val filteredMovies = raw.movies.filter { movie ->
            val matchesStatus = filter.selectedStatuses.isEmpty() || movie.status in filter.selectedStatuses
            val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                genreIdsByMovieId[movie.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
            matchesStatus && matchesGenre
        }

        val grouped = WatchStatus.entries
            .associateWith { status -> filteredMovies.filter { it.status == status } }
            .filterValues { it.isNotEmpty() }
            .mapValues { (_, movies) ->
                movies.map { movie ->
                    FilmListItem(
                        id = movie.tmdbId,
                        title = movie.title,
                        posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                        progress = if (movie.watched) 1f else null,
                        status = movie.status,
                    )
                }
            }

        val today = LocalDate.now()
        val upcoming = raw.movies
            .mapNotNull { movie ->
                val releaseDateStr = movie.releaseDate ?: return@mapNotNull null
                val releaseDate = runCatching { LocalDate.parse(releaseDateStr) }.getOrNull() ?: return@mapNotNull null
                if (releaseDate.isBefore(today)) return@mapNotNull null
                UpcomingMovieItem(
                    movieId = movie.tmdbId,
                    title = movie.title,
                    posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                    releaseDate = releaseDateStr,
                    daysUntil = ChronoUnit.DAYS.between(today, releaseDate),
                )
            }
            .sortedBy { it.releaseDate }

        FilmsUiState(
            isLoading = false,
            viewMode = filter.viewMode,
            groupedItems = grouped,
            upcoming = upcoming,
            availableGenres = raw.genres.map { GenreOption(it.tmdbId, it.name) },
            selectedStatuses = filter.selectedStatuses,
            selectedGenreIds = filter.selectedGenreIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilmsUiState(),
    )

    fun onViewModeToggled() {
        filterState.update { it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID) }
    }

    fun onFiltersApplied(statuses: Set<WatchStatus>, genreIds: Set<Int>) {
        filterState.update { it.copy(selectedStatuses = statuses, selectedGenreIds = genreIds) }
    }
}
