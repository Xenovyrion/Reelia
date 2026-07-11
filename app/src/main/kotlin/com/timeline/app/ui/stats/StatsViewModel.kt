package com.timeline.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalHoursWatched: Double = 0.0,
    val totalWatchedCount: Int = 0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    statsRepository: StatsRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = statsRepository.getBasicStats()
        .map { stats ->
            StatsUiState(
                totalHoursWatched = stats.totalMinutesWatched / 60.0,
                totalWatchedCount = stats.totalWatchedCount,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(),
        )
}
