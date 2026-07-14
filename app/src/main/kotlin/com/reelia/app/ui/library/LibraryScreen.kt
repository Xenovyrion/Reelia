package com.reelia.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.reelia.app.R
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.displayLabel
import com.reelia.app.ui.common.components.FilterBottomSheet
import com.reelia.app.ui.common.components.LibrarySortOption
import com.reelia.app.ui.common.components.MediaListRow
import com.reelia.app.ui.common.components.PosterCard
import com.reelia.app.ui.common.components.SectionHeader
import com.reelia.app.ui.common.components.UpcomingMovieCard
import com.reelia.app.ui.common.components.UpcomingShowCard
import com.reelia.app.ui.common.components.ViewMode
import com.reelia.app.ui.navigation.Routes
import com.reelia.app.ui.navigation.ScrollToTopOnTabReselect
import com.reelia.app.ui.theme.timeLineTopAppBarColors

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
    var showLocalSearch by remember { mutableStateOf(false) }
    // Kept as local state rather than reading straight from uiState.searchQuery: the latter only
    // catches up once the ViewModel's combine() pipeline re-derives it, which lags a typed
    // keystroke by at least a frame — driving the field from it directly made fast typing (and
    // backspacing down to empty) look like it wasn't registering.
    var searchText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val route = if (fixedMediaType == MediaType.TV) Routes.SERIES else Routes.FILMS

    ScrollToTopOnTabReselect(route) {
        if (uiState.viewMode == ViewMode.GRID) gridState.scrollToItem(0) else listState.scrollToItem(0)
    }

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
                    IconButton(
                        onClick = {
                            showLocalSearch = !showLocalSearch
                            if (!showLocalSearch) {
                                searchText = ""
                                viewModel.onSearchQueryChanged("")
                            }
                        },
                    ) {
                        Icon(
                            if (showLocalSearch) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = stringResource(R.string.library_local_search_content_description),
                        )
                    }
                    // A distinct "+" rather than another magnifying glass — this one leaves the
                    // library to search TMDB for new titles to add, not filter the current list.
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.library_add_content_description),
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.library_filter_sort_content_description),
                        )
                    }
                    IconButton(onClick = viewModel::onViewModeToggled) {
                        Icon(
                            if (uiState.viewMode == ViewMode.GRID) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.action_toggle_view_content_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val isEmpty = uiState.sections.isEmpty() && uiState.upcomingShows.isEmpty() && uiState.upcomingMovies.isEmpty()
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showLocalSearch) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    placeholder = { Text(stringResource(R.string.library_local_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchText = ""
                                    viewModel.onSearchQueryChanged("")
                                },
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.filter_reset_button))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (isEmpty && searchText.isNotBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.library_local_search_no_results, searchText),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (isEmpty) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(emptyStateRes),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (uiState.viewMode == ViewMode.LIST) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
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

                        uiState.sections.forEach { section ->
                            section.header?.let { header ->
                                item(key = "header_$header") {
                                    SectionHeader(header.label(), modifier = Modifier.padding(16.dp))
                                }
                            }
                            items(section.items, key = { "list_${it.mediaType}_${it.id}" }) { item ->
                                MediaListRow(
                                    title = item.title,
                                    subtitle = libraryItemSubtitle(item),
                                    posterUrl = item.posterUrl,
                                    episodeCode = item.nextEpisodeCode,
                                    onClick = { onItemClick(item.mediaType, item.id) },
                                )
                            }
                        }
                    }
                } else {
                    // A real grid (rather than manually chunking rows into a LazyColumn) lets
                    // Compose window and key each poster individually, so small filter/sort
                    // changes only recompose the cells that actually moved instead of whole
                    // 3-item rows.
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (uiState.upcomingShows.isNotEmpty() || uiState.upcomingMovies.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(
                                    stringResource(R.string.section_upcoming),
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(uiState.upcomingShows, key = { "show_${it.showId}" }) { UpcomingShowCard(it) }
                                    items(uiState.upcomingMovies, key = { "movie_${it.movieId}" }) { UpcomingMovieCard(it) }
                                }
                            }
                        }

                        uiState.sections.forEach { section ->
                            section.header?.let { header ->
                                item(span = { GridItemSpan(maxLineSpan) }, key = "header_$header") {
                                    SectionHeader(header.label(), modifier = Modifier.padding(vertical = 12.dp))
                                }
                            }
                            items(section.items, key = { "grid_${it.mediaType}_${it.id}" }) { item ->
                                PosterCard(
                                    title = item.title,
                                    posterUrl = item.posterUrl,
                                    status = item.status,
                                    progress = item.progress,
                                    isFavorite = item.isFavorite,
                                    onClick = { onItemClick(item.mediaType, item.id) },
                                )
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
            sortOption = uiState.sortOption,
            onApply = { statuses, genreIds, sortOption ->
                viewModel.onFiltersApplied(statuses, genreIds, sortOption)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun LibrarySectionHeader.label(): String = when (this) {
    is LibrarySectionHeader.Status -> status.displayLabel()
    is LibrarySectionHeader.Genre -> name
    is LibrarySectionHeader.NoGenre -> stringResource(R.string.library_sort_genre_other)
    is LibrarySectionHeader.Alpha -> letter
    is LibrarySectionHeader.SortLabel -> stringResource(
        when (sortOption) {
            LibrarySortOption.RECENTLY_ADDED -> R.string.library_sort_recently_added
            LibrarySortOption.RECENTLY_WATCHED -> R.string.library_sort_recently_watched
            else -> R.string.library_sort_recently_added // unreachable — only these two build SortLabel
        },
    )
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
