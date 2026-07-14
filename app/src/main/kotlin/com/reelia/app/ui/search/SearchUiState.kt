package com.reelia.app.ui.search

import androidx.annotation.StringRes
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.common.components.GenreOption

data class SearchResultItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val date: String?,
    val genreIds: List<Int> = emptyList(),
)

data class AddedItem(val mediaType: MediaType, val id: Int)

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoadingFeed: Boolean = true,
    val results: List<SearchResultItem> = emptyList(),
    val trendingFeed: List<SearchResultItem> = emptyList(),
    val hasApiKey: Boolean = true,
    val lockedMediaType: MediaType? = null,
    val availableGenres: List<GenreOption> = emptyList(),
    val selectedGenreIds: Set<Int> = emptySet(),
    val addingItems: Set<Pair<MediaType, Int>> = emptySet(),
    @StringRes val errorMessageRes: Int? = null,
) {
    val displayedItems: List<SearchResultItem>
        get() {
            val base = if (query.isBlank()) trendingFeed else results
            return if (selectedGenreIds.isEmpty()) {
                base
            } else {
                base.filter { item -> item.genreIds.any { it in selectedGenreIds } }
            }
        }
}
