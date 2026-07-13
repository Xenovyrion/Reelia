package com.timeline.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.displayLabel
import com.timeline.app.ui.common.components.FilterBottomSheet
import com.timeline.app.ui.common.components.MediaListRow
import com.timeline.app.ui.common.components.PosterCard
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.UpcomingMovieCard
import com.timeline.app.ui.common.components.UpcomingShowCard
import com.timeline.app.ui.common.components.ViewMode
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    fixedMediaType: MediaType,
    onItemClick: (MediaType, Int) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(fixedMediaType) {
        viewModel.onTypeFilterSelected(
            when (fixedMediaType) {
                MediaType.TV -> LibraryTypeFilter.SERIES
                MediaType.MOVIE -> LibraryTypeFilter.FILMS
            },
        )
    }

    val titleRes = when (fixedMediaType) {
        MediaType.TV -> R.string.series_title
        MediaType.MOVIE -> R.string.films_title
    }
    val emptyStateRes = when (fixedMediaType) {
        MediaType.TV -> R.string.series_empty_state
        MediaType.MOVIE -> R.string.films_empty_state
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                colors = timeLineTopAppBarColors(),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(R.string.library_search_content_description),
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.action_filter_content_description),
                        )
                    }
                    IconButton(onClick = viewModel::onViewModeToggled) {
                        Icon(
                            if (uiState.viewMode == ViewMode.GRID) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.action_toggle_view_content_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isEmpty = uiState.groupedItems.isEmpty() && uiState.upcomingShows.isEmpty() && uiState.upcomingMovies.isEmpty()
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (isEmpty) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(emptyStateRes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (uiState.upcomingShows.isNotEmpty() || uiState.upcomingMovies.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.section_upcoming), modifier = Modifier.padding(16.dp)) }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(uiState.upcomingShows, key = { "show_${it.showId}" }) { UpcomingShowCard(it) }
                                items(uiState.upcomingMovies, key = { "movie_${it.movieId}" }) { UpcomingMovieCard(it) }
                            }
                        }
                    }

                    uiState.groupedItems.forEach { (status, sectionItems) ->
                        item(key = "header_$status") {
                            SectionHeader(status.displayLabel(), modifier = Modifier.padding(16.dp))
                        }
                        if (uiState.viewMode == ViewMode.LIST) {
                            items(sectionItems, key = { "list_${it.mediaType}_${it.id}" }) { item ->
                                MediaListRow(
                                    title = item.title,
                                    subtitle = libraryItemSubtitle(item),
                                    posterUrl = item.posterUrl,
                                    episodeCode = item.nextEpisodeCode,
                                    onClick = { onItemClick(item.mediaType, item.id) },
                                )
                            }
                        } else {
                            items(sectionItems.chunked(3), key = { row -> "grid_${row.first().mediaType}_${row.first().id}" }) { row ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    row.forEach { item ->
                                        PosterCard(
                                            title = item.title,
                                            posterUrl = item.posterUrl,
                                            status = item.status,
                                            progress = item.progress,
                                            isFavorite = item.isFavorite,
                                            onClick = { onItemClick(item.mediaType, item.id) },
                                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                                        )
                                    }
                                    repeat(3 - row.size) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedStatuses = uiState.selectedStatuses,
            availableGenres = uiState.availableGenres,
            selectedGenreIds = uiState.selectedGenreIds,
            onApply = { statuses, genreIds ->
                viewModel.onFiltersApplied(statuses, genreIds)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun libraryItemSubtitle(item: LibraryItem): String? = when (item.mediaType) {
    MediaType.TV -> item.nextEpisodeName
    MediaType.MOVIE -> {
        val parts = listOfNotNull(
            item.runtimeMinutes?.let { stringResource(R.string.movie_detail_runtime_minutes_format, it) },
            item.genreNames.takeIf { it.isNotEmpty() }?.joinToString(", "),
        )
        parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }
}
