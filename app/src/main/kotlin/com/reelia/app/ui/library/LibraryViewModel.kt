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
import com.reelia.app.data.repository.StatsRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.domain.model.displayLabel
import com.reelia.app.ui.common.effectiveMovieStatus
import com.reelia.app.ui.common.effectiveShowStatus
import com.reelia.app.ui.common.components.GenreOption
import com.reelia.app.ui.common.components.LibrarySortOption
import com.reelia.app.ui.common.components.ViewMode
import com.reelia.app.ui.common.model.buildUpcomingMovieItems
import com.reelia.app.ui.common.model.buildUpcomingShowItems
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
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
    val sortOption: LibrarySortOption = LibrarySortOption.STATUS,
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
    private val statsRepository: StatsRepository,
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
        statsRepository.getLastWatchedPerMedia(),
        showRepository.getAllEpisodeNames(),
    ) { showData, movieData, filter, lastWatchedEntries, episodeNames ->
        val lastWatchedByKey: Map<Pair<MediaType, Int>, Instant> =
            lastWatchedEntries.associate { (it.mediaType to it.tmdbId) to it.watchedAt }
        val episodeNamesByShowId: Map<Int, List<String>> =
            episodeNames.groupBy({ it.showId }, { it.name })
        val query = filter.searchQuery.trim()
        val genreIdsByShowId = showData.crossRefs.groupBy({ it.showId }, { it.genreId })
        val genreIdsByMovieId = movieData.crossRefs.groupBy({ it.movieId }, { it.genreId })
        val showGenreNameById = showData.genres.associateBy { it.tmdbId }
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
                    val matchesQuery = query.isEmpty() ||
                        show.name.contains(query, ignoreCase = true) ||
                        episodeNamesByShowId[show.tmdbId].orEmpty().any { it.contains(query, ignoreCase = true) }
                    matchesStatus && matchesGenre && matchesQuery
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
                        // Watch-log entries survive remove/re-add (kept for stats accuracy — see
                        // ShowRepository.removeShow) and TV Time import, so a show can carry watch
                        // history from before its current stay in the library. Only count it for
                        // the "recently watched" sort if it happened after this add.
                        lastWatchedAt = lastWatchedByKey[MediaType.TV to show.tmdbId]?.takeIf { it >= show.addedAt },
                        nextEpisodeCode = nextEpisode?.let { "S${it.seasonNumber} · E${it.episodeNumber}" },
                        nextEpisodeName = nextEpisode?.name,
                        genreNames = genreIdsByShowId[show.tmdbId].orEmpty().mapNotNull { showGenreNameById[it]?.name },
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
                    val matchesQuery = query.isEmpty() || movie.title.contains(query, ignoreCase = true)
                    matchesStatus && matchesGenre && matchesQuery
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
                        lastWatchedAt = lastWatchedByKey[MediaType.MOVIE to movie.tmdbId]?.takeIf { it >= movie.addedAt },
                        runtimeMinutes = movie.runtimeMinutes,
                        genreNames = genreIdsByMovieId[movie.tmdbId].orEmpty().mapNotNull { movieGenreNameById[it]?.name },
                    )
                }
        }

        val combinedItems = showItems + movieItems
        val sections = buildSections(combinedItems, filter.sortOption)

        // Upcoming episodes/releases aren't part of what's being searched for, so they'd just be
        // noise mixed in with search results — hidden while a query is active.
        val upcomingShows = if (filter.typeFilter == LibraryTypeFilter.FILMS || query.isNotEmpty()) {
            emptyList()
        } else {
            buildUpcomingShowItems(showData.shows, imageUrlBuilder)
        }

        val upcomingMovies = if (filter.typeFilter == LibraryTypeFilter.SERIES || query.isNotEmpty()) {
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
            sortOption = filter.sortOption,
            sections = sections,
            upcomingShows = upcomingShows,
            upcomingMovies = upcomingMovies,
            availableGenres = availableGenres,
            selectedStatuses = filter.selectedStatuses,
            selectedGenreIds = filter.selectedGenreIds,
            searchQuery = filter.searchQuery,
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

    fun onFiltersApplied(statuses: Set<WatchStatus>, genreIds: Set<Int>, sortOption: LibrarySortOption) {
        filterState.update { it.copy(selectedStatuses = statuses, selectedGenreIds = genreIds, sortOption = sortOption) }
    }

    fun onSearchQueryChanged(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

}

private fun alphaSorted(items: List<LibraryItem>): List<LibraryItem> = items.sortedBy { it.title.lowercase() }

/** First-letter index bucket for the alphabetical sort, e.g. a contacts app's section list —
 * digits fold into one "0-9" bucket rather than one section per digit. */
private fun LibraryItem.alphaBucket(): String {
    val firstChar = title.trim().firstOrNull() ?: return "#"
    return if (firstChar.isDigit()) "0-9" else firstChar.uppercaseChar().toString()
}

private fun buildSections(items: List<LibraryItem>, sortOption: LibrarySortOption): List<LibrarySection> =
    when (sortOption) {
        LibrarySortOption.STATUS -> WatchStatus.entries
            .mapNotNull { status ->
                val statusItems = alphaSorted(items.filter { it.status == status })
                statusItems.takeIf { it.isNotEmpty() }?.let { LibrarySection(LibrarySectionHeader.Status(status), it) }
            }
        LibrarySortOption.ALPHABETICAL -> alphaSorted(items)
            .groupBy { it.alphaBucket() }
            .map { (letter, bucketItems) -> LibrarySection(LibrarySectionHeader.Alpha(letter), bucketItems) }
        LibrarySortOption.RECENTLY_ADDED -> listOf(
            LibrarySection(
                header = null,
                items = items.sortedWith(compareByDescending<LibraryItem> { it.addedAt }.thenBy { it.title.lowercase() }),
            ),
        )
        LibrarySortOption.RECENTLY_WATCHED -> listOf(
            LibrarySection(
                header = null,
                items = items.sortedWith(
                    compareByDescending<LibraryItem> { it.lastWatchedAt ?: Instant.MIN }.thenBy { it.title.lowercase() },
                ),
            ),
        )
        LibrarySortOption.GENRE -> items
            .groupBy { it.genreNames.firstOrNull() }
            .toSortedMap(compareBy { it ?: "￿" }) // null (no genre) bucket sorts last
            .map { (genreName, genreItems) ->
                val header = genreName?.let { LibrarySectionHeader.Genre(it) } ?: LibrarySectionHeader.NoGenre
                LibrarySection(header, alphaSorted(genreItems))
            }
    }
