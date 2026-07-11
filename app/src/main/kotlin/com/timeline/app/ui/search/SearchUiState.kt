package com.timeline.app.ui.search

import com.timeline.app.domain.model.MediaType

data class SearchResultItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val date: String?,
)

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoadingFeed: Boolean = true,
    val results: List<SearchResultItem> = emptyList(),
    val trendingFeed: List<SearchResultItem> = emptyList(),
    val hasApiKey: Boolean = true,
    val errorMessage: String? = null,
) {
    val displayedItems: List<SearchResultItem>
        get() = if (query.isBlank()) trendingFeed else results
}
