package com.reelia.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.local.dao.ShowEpisodeProgress
import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.GenreEntity
import com.reelia.app.data.local.entity.MovieGenreCrossRef
import com.reelia.app.data.local.entity.ShowGenreCrossRef
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.effectiveMovieStatus
import com.reelia.app.ui.common.effectiveShowStatus
import com.reelia.app.ui.common.components.GenreOption
import com.reelia.app.ui.common.components.ViewMode
import com.reelia.app.ui.common.model.buildUpcomingMovieItems
import com.reelia.app.ui.common.model.buildUpcomingShowItems
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class RawShowData(
    val shows: List<TrackedShowEntity>,
    val progress: List<ShowEpisodeProgress>,
    val genres: List<GenreEntity>,
    val crossRefs: List<ShowGenreCrossRef>,
    val unwatchedEpisodes: List<EpisodeEntity>,
)

private data class RawMovieData(
    val movies: List<TrackedMovieEntity>,
    val genres: List<GenreEntity>,
    val crossRefs: List<MovieGenreCrossRef>,
)

private data class LibraryFilterState(
    val viewMode: ViewMode = ViewMode.GRID,
    val typeFilter: LibraryTypeFilter = LibraryTypeFilter.ALL,
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val filterState = MutableStateFlow(LibraryFilterState())

    private val rawShowData = combine(
        showRepository.getAllShows(),
        showRepository.getEpisodeProgressByShow(),
        showRepository.getGenresForTrackedShows(),
        showRepository.getShowGenreCrossRefs(),
        showRepository.getAllUnwatchedEpisodesOrdered(),
    ) { shows, progress, genres, crossRefs, unwatchedEpisodes ->
        RawShowData(shows, progress, genres, crossRefs, unwatchedEpisodes)
    }

    private val rawMovieData = combine(
        movieRepository.getAllMovies(),
        movieRepository.getGenresForTrackedMovies(),
        movieRepository.getMovieGenreCrossRefs(),
    ) { movies, genres, crossRefs -> RawMovieData(movies, genres, crossRefs) }

    val uiState: StateFlow<LibraryUiState> = combine(
        rawShowData,
        rawMovieData,
        filterState,
    ) { showData, movieData, filter ->
        val genreIdsByShowId = showData.crossRefs.groupBy({ it.showId }, { it.genreId })
        val genreIdsByMovieId = movieData.crossRefs.groupBy({ it.movieId }, { it.genreId })
        val movieGenreNameById = movieData.genres.associateBy { it.tmdbId }
        val progressByShowId = showData.progress.associateBy { it.showId }
        val nextEpisodeByShowId = showData.unwatchedEpisodes.groupBy { it.showId }.mapValues { it.value.first() }

        val showItems = if (filter.typeFilter == LibraryTypeFilter.FILMS) {
            emptyList()
        } else {
            showData.shows
                .map { show -> show to effectiveShowStatus(show.status, progressByShowId[show.tmdbId]) }
                .filter { (show, status) ->
                    val matchesStatus = filter.selectedStatuses.isEmpty() || status in filter.selectedStatuses
                    val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                        genreIdsByShowId[show.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
                    matchesStatus && matchesGenre
                }
                .map { (show, status) ->
                    val showProgress = progressByShowId[show.tmdbId]
                    val nextEpisode = nextEpisodeByShowId[show.tmdbId]
                    LibraryItem(
                        id = show.tmdbId,
                        mediaType = MediaType.TV,
                        title = show.name,
                        posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                        progress = showProgress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total },
                        status = status,
                        isFavorite = show.isFavorite,
                        addedAt = show.addedAt,
                        nextEpisodeCode = nextEpisode?.let { "S${it.seasonNumber} · E${it.episodeNumber}" },
                        nextEpisodeName = nextEpisode?.name,
                    )
                }
        }

        val movieItems = if (filter.typeFilter == LibraryTypeFilter.SERIES) {
            emptyList()
        } else {
            movieData.movies
                .map { movie -> movie to effectiveMovieStatus(movie.status, movie.watched) }
                .filter { (movie, status) ->
                    val matchesStatus = filter.selectedStatuses.isEmpty() || status in filter.selectedStatuses
                    val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                        genreIdsByMovieId[movie.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
                    matchesStatus && matchesGenre
                }
                .map { (movie, status) ->
                    LibraryItem(
                        id = movie.tmdbId,
                        mediaType = MediaType.MOVIE,
                        title = movie.title,
                        posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                        progress = if (movie.watched) 1f else null,
                        status = status,
                        isFavorite = movie.isFavorite,
                        addedAt = movie.addedAt,
                        runtimeMinutes = movie.runtimeMinutes,
                        genreNames = genreIdsByMovieId[movie.tmdbId].orEmpty().mapNotNull { movieGenreNameById[it]?.name },
                    )
                }
        }

        val combinedItems = showItems + movieItems
        val grouped = WatchStatus.entries
            .associateWith { status -> combinedItems.filter { it.status == status } }
            .filterValues { it.isNotEmpty() }

        val upcomingShows = if (filter.typeFilter == LibraryTypeFilter.FILMS) {
            emptyList()
        } else {
            buildUpcomingShowItems(showData.shows, imageUrlBuilder)
        }

        val upcomingMovies = if (filter.typeFilter == LibraryTypeFilter.SERIES) {
            emptyList()
        } else {
            buildUpcomingMovieItems(movieData.movies, imageUrlBuilder)
        }

        val availableGenres = (showData.genres + movieData.genres)
            .distinctBy { it.tmdbId }
            .sortedBy { it.name }
            .map { GenreOption(it.tmdbId, it.name) }

        LibraryUiState(
            isLoading = false,
            viewMode = filter.viewMode,
            typeFilter = filter.typeFilter,
            groupedItems = grouped,
            upcomingShows = upcomingShows,
            upcomingMovies = upcomingMovies,
            availableGenres = availableGenres,
            selectedStatuses = filter.selectedStatuses,
            selectedGenreIds = filter.selectedGenreIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onViewModeToggled() {
        filterState.update { it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID) }
    }

    fun onTypeFilterSelected(typeFilter: LibraryTypeFilter) {
        filterState.update { it.copy(typeFilter = typeFilter) }
    }

    fun onFiltersApplied(statuses: Set<WatchStatus>, genreIds: Set<Int>) {
        filterState.update { it.copy(selectedStatuses = statuses, selectedGenreIds = genreIds) }
    }
}
