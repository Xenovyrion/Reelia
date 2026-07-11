package com.timeline.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.repository.StatsRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.common.components.BarChartEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

data class StatsUiState(
    val scope: StatsScope = StatsScope.ALL,
    val totalHoursWatched: Double = 0.0,
    val totalWatchedCount: Int = 0,
    val weeklyChart: List<BarChartEntry> = emptyList(),
    val monthlyChart: List<BarChartEntry> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val scopeState = MutableStateFlow(StatsScope.ALL)

    val uiState: StateFlow<StatsUiState> = scopeState.flatMapLatest { scope ->
        val mediaType = scope.toMediaType()
        combine(
            statsRepository.getBasicStats(mediaType),
            statsRepository.getWeeklyBreakdown(mediaType),
            statsRepository.getMonthlyBreakdown(mediaType),
        ) { basic, weekly, monthly ->
            StatsUiState(
                scope = scope,
                totalHoursWatched = basic.totalMinutesWatched / 60.0,
                totalWatchedCount = basic.totalWatchedCount,
                weeklyChart = weekly.reversed().map { BarChartEntry(it.bucket.takeLast(3), it.totalMinutes / 60f) },
                monthlyChart = monthly.reversed().map { BarChartEntry(it.bucket.takeLast(2), it.totalMinutes / 60f) },
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
