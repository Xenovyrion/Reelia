package com.reelia.app.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.reelia.app.R
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.statusColor
import com.reelia.app.ui.common.components.CircularProgressRing
import com.reelia.app.ui.common.components.EpisodeCodeBadge
import com.reelia.app.ui.common.components.SectionHeader
import com.reelia.app.ui.common.components.UpcomingMovieCard
import com.reelia.app.ui.common.components.UpcomingShowCard
import com.reelia.app.ui.navigation.Routes
import com.reelia.app.ui.navigation.ScrollToTopOnTabReselect

@Composable
fun HomeScreen(
    onShowClick: (Int) -> Unit,
    onDiscoverItemClick: (MediaType, Int) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    ScrollToTopOnTabReselect(Routes.HOME) { listState.animateScrollToItem(0) }

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val isEmpty = uiState.continueWatching.isEmpty() &&
            uiState.upcomingShows.isEmpty() &&
            uiState.upcomingMovies.isEmpty() &&
            uiState.trending.isEmpty() &&
            uiState.recentMovies.isEmpty() &&
            uiState.recentShows.isEmpty() &&
            uiState.suggestions.isEmpty() &&
            uiState.favoriteShows.isEmpty() &&
            uiState.favoriteMovies.isEmpty()
        if (isEmpty && uiState.isDiscoverLoading) {
            // The discovery feeds (trending/recent/suggestions) are still in flight — show the
            // spinner rather than a premature "empty library" message that would just flash by.
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (isEmpty) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.home_empty_state),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                val periodRes = when (uiState.greetingPeriod) {
                    GreetingPeriod.MORNING -> R.string.home_greeting_morning
                    GreetingPeriod.AFTERNOON -> R.string.home_greeting_afternoon
                    GreetingPeriod.EVENING -> R.string.home_greeting_evening
                }
                val greeting = uiState.userFirstName?.let {
                    stringResource(R.string.home_greeting_with_name_format, stringResource(periodRes), it)
                } ?: stringResource(periodRes)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        greeting,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(R.string.library_search_content_description),
                        )
                    }
                }
            }

            if (uiState.upcomingShows.isNotEmpty() || uiState.upcomingMovies.isNotEmpty()) {
                item {
                    SectionHeader(
                        stringResource(R.string.section_upcoming),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
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

            discoverSection(
                titleRes = R.string.home_favorite_shows_section_title,
                items = uiState.favoriteShows,
                onItemClick = onDiscoverItemClick,
            )
            discoverSection(
                titleRes = R.string.home_favorite_movies_section_title,
                items = uiState.favoriteMovies,
                onItemClick = onDiscoverItemClick,
            )
            discoverSection(
                titleRes = R.string.home_suggestions_section_title,
                items = uiState.suggestions,
                onItemClick = onDiscoverItemClick,
            )
            discoverSection(
                titleRes = R.string.home_trending_section_title,
                items = uiState.trending,
                onItemClick = onDiscoverItemClick,
            )
            discoverSection(
                titleRes = R.string.home_recent_movies_section_title,
                items = uiState.recentMovies,
                onItemClick = onDiscoverItemClick,
            )
            discoverSection(
                titleRes = R.string.home_recent_shows_section_title,
                items = uiState.recentShows,
                onItemClick = onDiscoverItemClick,
            )

            item { Box(Modifier.padding(bottom = 16.dp)) }
        }
    }
}

private fun LazyListScope.discoverSection(
    @StringRes titleRes: Int,
    items: List<HomeDiscoverItem>,
    onItemClick: (MediaType, Int) -> Unit,
) {
    if (items.isEmpty()) return
    item {
        SectionHeader(
            stringResource(titleRes),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
    item {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { "${it.mediaType}_${it.tmdbId}" }) { item ->
                DiscoverPosterCard(item, onClick = { onItemClick(item.mediaType, item.tmdbId) })
            }
        }
    }
}

@Composable
private fun DiscoverPosterCard(item: HomeDiscoverItem, onClick: () -> Unit) {
    Column(modifier = Modifier.width(110.dp).clickable(onClick = onClick)) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Text(
            item.title,
            style = MaterialTheme.typography.labelMedium,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            item.year.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        // A bright/light backdrop can make the ring and text unreadable without this — the
        // scrim guarantees contrast no matter what the underlying image looks like.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.85f),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressRing(
                progress = item.progress,
                color = item.status.statusColor(),
            )
        }
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
