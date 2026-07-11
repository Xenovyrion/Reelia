package com.timeline.app.ui.films

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.displayLabel
import com.timeline.app.ui.common.components.FilterBottomSheet
import com.timeline.app.ui.common.components.MediaListRow
import com.timeline.app.ui.common.components.PosterCard
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.UpcomingCountdownBadge
import com.timeline.app.ui.common.components.ViewMode
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmsScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: FilmsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.films_title)) },
                colors = timeLineTopAppBarColors(),
                actions = {
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
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.groupedItems.isEmpty() && uiState.upcoming.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.films_empty_state),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (uiState.upcoming.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.section_upcoming), modifier = Modifier.padding(16.dp)) }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.upcoming, key = { it.movieId }) { movie ->
                                UpcomingMovieCard(movie)
                            }
                        }
                    }
                }

                uiState.groupedItems.forEach { (status, sectionItems) ->
                    item(key = "header_$status") {
                        SectionHeader(status.displayLabel(), modifier = Modifier.padding(16.dp))
                    }
                    if (uiState.viewMode == ViewMode.LIST) {
                        items(sectionItems, key = { "list_${it.id}" }) { item ->
                            MediaListRow(
                                title = item.title,
                                subtitle = filmMetadataLine(item.runtimeMinutes, item.genreNames),
                                posterUrl = item.posterUrl,
                                onClick = { onMovieClick(item.id) },
                            )
                        }
                    } else {
                        items(sectionItems.chunked(3), key = { row -> "grid_${row.first().id}" }) { row ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                                row.forEach { item ->
                                    PosterCard(
                                        title = item.title,
                                        posterUrl = item.posterUrl,
                                        status = item.status,
                                        progress = item.progress,
                                        onClick = { onMovieClick(item.id) },
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
private fun UpcomingMovieCard(movie: UpcomingMovieItem) {
    Card(modifier = Modifier.width(240.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(56.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    movie.releaseDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            UpcomingCountdownBadge(movie.daysUntil, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun filmMetadataLine(runtimeMinutes: Int?, genreNames: List<String>): String? {
    val parts = listOfNotNull(
        runtimeMinutes?.let { stringResource(R.string.movie_detail_runtime_minutes_format, it) },
        genreNames.takeIf { it.isNotEmpty() }?.joinToString(", "),
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
