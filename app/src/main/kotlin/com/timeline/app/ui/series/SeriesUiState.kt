package com.timeline.app.ui.series

import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.GenreOption
import com.timeline.app.ui.common.components.ViewMode

data class SeriesListItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val progress: Float?,
    val status: WatchStatus,
)

data class UpcomingEpisodeItem(
    val showId: Int,
    val showTitle: String,
    val episodeName: String,
    val networkNames: String?,
    val airDate: String,
    val daysUntil: Long,
)

data class SeriesUiState(
    val isLoading: Boolean = true,
    val viewMode: ViewMode = ViewMode.GRID,
    val groupedItems: Map<WatchStatus, List<SeriesListItem>> = emptyMap(),
    val upcoming: List<UpcomingEpisodeItem> = emptyList(),
    val availableGenres: List<GenreOption> = emptyList(),
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)
