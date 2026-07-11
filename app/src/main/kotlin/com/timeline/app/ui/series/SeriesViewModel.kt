package com.timeline.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.local.dao.ShowEpisodeProgress
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.ShowGenreCrossRef
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.ShowRepository
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

private data class RawSeriesData(
    val shows: List<TrackedShowEntity>,
    val progress: List<ShowEpisodeProgress>,
    val genres: List<GenreEntity>,
    val crossRefs: List<ShowGenreCrossRef>,
)

private data class FilterState(
    val viewMode: ViewMode = ViewMode.GRID,
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())

    private val rawData = combine(
        showRepository.getAllShows(),
        showRepository.getEpisodeProgressByShow(),
        showRepository.getGenresForTrackedShows(),
        showRepository.getShowGenreCrossRefs(),
    ) { shows, progress, genres, crossRefs -> RawSeriesData(shows, progress, genres, crossRefs) }

    val uiState: StateFlow<SeriesUiState> = combine(rawData, filterState) { raw, filter ->
        val progressByShowId = raw.progress.associateBy { it.showId }
        val genreIdsByShowId = raw.crossRefs.groupBy({ it.showId }, { it.genreId })

        val filteredShows = raw.shows.filter { show ->
            val matchesStatus = filter.selectedStatuses.isEmpty() || show.status in filter.selectedStatuses
            val matchesGenre = filter.selectedGenreIds.isEmpty() ||
                genreIdsByShowId[show.tmdbId].orEmpty().any { it in filter.selectedGenreIds }
            matchesStatus && matchesGenre
        }

        val grouped = WatchStatus.entries
            .associateWith { status -> filteredShows.filter { it.status == status } }
            .filterValues { it.isNotEmpty() }
            .mapValues { (_, shows) ->
                shows.map { show ->
                    val showProgress = progressByShowId[show.tmdbId]
                    SeriesListItem(
                        id = show.tmdbId,
                        title = show.name,
                        posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                        progress = showProgress?.let {
                            if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total
                        },
                        status = show.status,
                    )
                }
            }

        val today = LocalDate.now()
        val upcoming = raw.shows
            .mapNotNull { show ->
                val airDateStr = show.nextEpisodeToAirDate ?: return@mapNotNull null
                val airDate = runCatching { LocalDate.parse(airDateStr) }.getOrNull() ?: return@mapNotNull null
                UpcomingEpisodeItem(
                    showId = show.tmdbId,
                    showTitle = show.name,
                    episodeName = show.nextEpisodeToAirName.orEmpty(),
                    networkNames = show.networkNames,
                    airDate = airDateStr,
                    daysUntil = ChronoUnit.DAYS.between(today, airDate),
                )
            }
            .sortedBy { it.airDate }

        SeriesUiState(
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
        initialValue = SeriesUiState(),
    )

    fun onViewModeToggled() {
        filterState.update { it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID) }
    }

    fun onFiltersApplied(statuses: Set<WatchStatus>, genreIds: Set<Int>) {
        filterState.update { it.copy(selectedStatuses = statuses, selectedGenreIds = genreIds) }
    }
}
