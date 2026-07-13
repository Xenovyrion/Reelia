package com.timeline.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.statusColor
import com.timeline.app.ui.common.components.CircularProgressRing
import com.timeline.app.ui.common.components.EpisodeCodeBadge
import com.timeline.app.ui.common.components.PosterCard
import com.timeline.app.ui.common.components.SectionHeader

@Composable
fun HomeScreen(
    onShowClick: (Int) -> Unit,
    onItemClick: (MediaType, Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.continueWatching.isEmpty() && uiState.libraryItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.home_empty_state),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Text(
                    stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }

            if (uiState.continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(
                        stringResource(R.string.home_continue_watching_title),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.continueWatching, key = { it.showId }) { item ->
                            ContinueWatchingCard(item, onClick = { onShowClick(item.showId) })
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    stringResource(R.string.home_library_section_title),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(uiState.libraryItems.chunked(3), key = { row -> "grid_${row.first().mediaType}_${row.first().id}" }) { row ->
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

@Composable
private fun ContinueWatchingCard(item: ContinueWatchingItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.backdropUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        CircularProgressRing(
            progress = item.progress,
            color = item.status.statusColor(),
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            EpisodeCodeBadge(
                seasonNumber = item.seasonNumber,
                episodeNumber = item.episodeNumber,
                color = item.status.statusColor(),
            )
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
