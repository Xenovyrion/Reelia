package com.timeline.app.ui.library

import com.timeline.app.domain.model.MediaType

data class LibraryItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
)

data class LibraryUiState(
    val isLoading: Boolean = true,
    val items: List<LibraryItem> = emptyList(),
)
