package com.timeline.app.ui.home

import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.library.LibraryItem

data class ContinueWatchingItem(
    val showId: Int,
    val title: String,
    val backdropUrl: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String,
    val progress: Float,
    val status: WatchStatus,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val libraryItems: List<LibraryItem> = emptyList(),
)
