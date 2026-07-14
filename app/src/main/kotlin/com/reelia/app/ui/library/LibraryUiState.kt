package com.reelia.app.ui.library

import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.components.GenreOption
import com.reelia.app.ui.common.components.ViewMode
import com.reelia.app.ui.common.model.UpcomingMovieItem
import com.reelia.app.ui.common.model.UpcomingShowItem
import java.time.Instant

enum class LibraryTypeFilter { ALL, SERIES, FILMS }

data class LibraryItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val progress: Float?,
    val status: WatchStatus,
    val isFavorite: Boolean,
    val addedAt: Instant,
    val nextEpisodeCode: String? = null,
    val nextEpisodeName: String? = null,
    val runtimeMinutes: Int? = null,
    val genreNames: List<String> = emptyList(),
)

data class LibraryUiState(
    val isLoading: Boolean = true,
    val viewMode: ViewMode = ViewMode.GRID,
    val typeFilter: LibraryTypeFilter = LibraryTypeFilter.ALL,
    val groupedItems: Map<WatchStatus, List<LibraryItem>> = emptyMap(),
    val upcomingShows: List<UpcomingShowItem> = emptyList(),
    val upcomingMovies: List<UpcomingMovieItem> = emptyList(),
    val availableGenres: List<GenreOption> = emptyList(),
    val selectedStatuses: Set<WatchStatus> = emptySet(),
    val selectedGenreIds: Set<Int> = emptySet(),
)
