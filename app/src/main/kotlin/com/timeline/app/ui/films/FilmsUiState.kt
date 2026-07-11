package com.timeline.app.ui.films

import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.GenreOption
import com.timeline.app.ui.common.components.ViewMode

data class FilmListItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val progress: Float?,
    val status: WatchStatus,
    val runtimeMinutes: Int? = null,
    val genreNames: List<String> = emptyList(),
)

data class UpcomingMovieItem(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String,
    val daysUntil: Long,
)

data class FilmsUiState(
    val isLoading: Boolean = true,
    val viewMode: ViewMode = ViewMode.GRID,
    val groupedItems: Map<WatchStatus, List<FilmListItem>> = emptyMap(),
    val upcoming: List<UpcomingMovieItem> = emptyList(),
    val availableGenres: List<GenreOption> = emptyList(),
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)
