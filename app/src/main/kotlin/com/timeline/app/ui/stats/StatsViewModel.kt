package com.timeline.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.data.repository.StatsRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.BarChartEntry
import com.timeline.app.ui.common.components.GenreProgressItem
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class StatsScope { ALL, SERIES, FILMS }

private fun StatsScope.toMediaType(): MediaType? = when (this) {
    StatsScope.ALL -> null
    StatsScope.SERIES -> MediaType.TV
    StatsScope.FILMS -> MediaType.MOVIE
}

private data class CompletionData(val totalCount: Int, val completedCount: Int)

private val GenrePalette = listOf(StatusWatchingCompleted, StatusWantToWatch, StatusPlanned, StatusFavorite)

data class StatsUiState(
    val scope: StatsScope = StatsScope.ALL,
    val totalHoursWatched: Double = 0.0,
    val totalWatchedCount: Int = 0,
    val weeklyChart: List<BarChartEntry> = emptyList(),
    val monthlyChart: List<BarChartEntry> = emptyList(),
    val genreBreakdown: List<GenreProgressItem> = emptyList(),
    val completedCount: Int = 0,
    val completedFraction: Float = 0f,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val scopeState = MutableStateFlow(StatsScope.ALL)

    private fun completionFlow(scope: StatsScope): Flow<CompletionData> =
        combine(showRepository.getAllShows(), movieRepository.getAllMovies()) { shows, movies ->
            val statuses = when (scope) {
                StatsScope.ALL -> shows.map { it.status } + movies.map { it.status }
                StatsScope.SERIES -> shows.map { it.status }
                StatsScope.FILMS -> movies.map { it.status }
            }
            CompletionData(totalCount = statuses.size, completedCount = statuses.count { it == WatchStatus.COMPLETED })
        }

    val uiState: StateFlow<StatsUiState> = scopeState.flatMapLatest { scope ->
        val mediaType = scope.toMediaType()
        combine(
            statsRepository.getBasicStats(mediaType),
            statsRepository.getWeeklyBreakdown(mediaType),
            statsRepository.getMonthlyBreakdown(mediaType),
            statsRepository.getGenreBreakdown(mediaType, limit = 5),
            completionFlow(scope),
        ) { basic, weekly, monthly, genres, completion ->
            val totalGenreMinutes = genres.sumOf { it.totalMinutes }
            StatsUiState(
                scope = scope,
                totalHoursWatched = basic.totalMinutesWatched / 60.0,
                totalWatchedCount = basic.totalWatchedCount,
                weeklyChart = weekly.reversed().map { BarChartEntry(it.bucket.takeLast(3), it.totalMinutes / 60f) },
                monthlyChart = monthly.reversed().map { BarChartEntry(it.bucket.takeLast(2), it.totalMinutes / 60f) },
                genreBreakdown = genres.mapIndexed { index, genre ->
                    GenreProgressItem(
                        name = genre.genreName,
                        fraction = if (totalGenreMinutes == 0) 0f else genre.totalMinutes.toFloat() / totalGenreMinutes,
                        color = GenrePalette[index % GenrePalette.size],
                    )
                },
                completedCount = completion.completedCount,
                completedFraction = if (completion.totalCount == 0) 0f else completion.completedCount.toFloat() / completion.totalCount,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    fun onScopeSelected(scope: StatsScope) {
        scopeState.value = scope
    }
}
