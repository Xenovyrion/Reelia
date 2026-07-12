package com.timeline.app.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.local.dao.ShowEpisodeProgress
import com.timeline.app.data.local.entity.EpisodeEntity
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.ShowGenreCrossRef
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
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
    savedStateHandle: SavedStateHandle,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    // Set only when reached via the "browse this genre" route from Profile's genre breakdown —
    // the plain bottom-nav Library tab has no genreId arg, so this is null there.
    private val initialGenreId: Int? = savedStateHandle.get<Int>("genreId")

    private val filterState = MutableStateFlow(
        LibraryFilterState(selectedGenreIds = initialGenreId?.let { setOf(it) } ?: emptySet()),
    )

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
                .filter { show ->
                    val matchesStatus = filter.selectedStatuses.isEmpty() || show.status in filter.selectedStatuses
                    val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                        genreIdsByShowId[show.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
                    matchesStatus && matchesGenre
                }
                .map { show ->
                    val showProgress = progressByShowId[show.tmdbId]
                    val nextEpisode = nextEpisodeByShowId[show.tmdbId]
                    LibraryItem(
                        id = show.tmdbId,
                        mediaType = MediaType.TV,
                        title = show.name,
                        posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                        progress = showProgress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total },
                        status = show.status,
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
                .filter { movie ->
                    val matchesStatus = filter.selectedStatuses.isEmpty() || movie.status in filter.selectedStatuses
                    val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                        genreIdsByMovieId[movie.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
                    matchesStatus && matchesGenre
                }
                .map { movie ->
                    LibraryItem(
                        id = movie.tmdbId,
                        mediaType = MediaType.MOVIE,
                        title = movie.title,
                        posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                        progress = if (movie.watched) 1f else null,
                        status = movie.status,
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

        val today = LocalDate.now()
        val upcomingShows = if (filter.typeFilter == LibraryTypeFilter.FILMS) {
            emptyList()
        } else {
            showData.shows
                .mapNotNull { show ->
                    val airDateStr = show.nextEpisodeToAirDate ?: return@mapNotNull null
                    val airDate = runCatching { LocalDate.parse(airDateStr) }.getOrNull() ?: return@mapNotNull null
                    UpcomingShowItem(
                        showId = show.tmdbId,
                        showTitle = show.name,
                        episodeName = show.nextEpisodeToAirName.orEmpty(),
                        networkNames = show.networkNames,
                        posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                        airDate = airDateStr,
                        daysUntil = ChronoUnit.DAYS.between(today, airDate),
                    )
                }
                .sortedBy { it.airDate }
        }

        val upcomingMovies = if (filter.typeFilter == LibraryTypeFilter.SERIES) {
            emptyList()
        } else {
            movieData.movies
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
