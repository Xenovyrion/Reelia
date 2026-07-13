package com.timeline.app.ui.showdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.ShowBroadcastStatus
import com.timeline.app.domain.model.displayLabel
import com.timeline.app.ui.common.components.BackdropHeader
import com.timeline.app.ui.common.components.CastRow
import com.timeline.app.ui.common.components.EpisodeCodeBadge
import com.timeline.app.ui.common.components.SeasonPillItem
import com.timeline.app.ui.common.components.SeasonPillTabs
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.WatchProvidersRow
import com.timeline.app.ui.common.components.WatchedToggleButton
import com.timeline.app.ui.theme.StatusFavorite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit,
    viewModel: ShowDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSeasonNumber by remember { mutableStateOf<Int?>(null) }
    var expandedEpisodes by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                BackdropHeader(
                    imageUrl = uiState.backdropUrl ?: uiState.posterUrl,
                    contentDescription = uiState.title,
                    onBack = onBack,
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            uiState.title,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.onFavoriteToggled(!uiState.isFavorite) }) {
                            Icon(
                                if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(R.string.detail_favorite_toggle_content_description),
                                tint = if (uiState.isFavorite) StatusFavorite else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.padding(top = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            listOfNotNull(
                                stringResource(R.string.season_count_format, uiState.seasonCount),
                                uiState.networkNames,
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        uiState.userRating?.let { rating ->
                            RatingBadge(rating)
                        }
                    }
                    Spacer(Modifier.padding(top = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BroadcastStatusPill(uiState.broadcastStatus)
                        Text(
                            uiState.status.displayLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    if (uiState.yearRange != null || uiState.genreNames.isNotEmpty()) {
                        Text(
                            listOfNotNull(
                                uiState.yearRange,
                                uiState.genreNames.takeIf { it.isNotEmpty() }?.joinToString(", "),
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(Modifier.padding(top = 12.dp))
                    val progress = if (uiState.totalEpisodeCount == 0) {
                        0f
                    } else {
                        uiState.watchedEpisodeCount.toFloat() / uiState.totalEpisodeCount
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                    Spacer(Modifier.padding(top = 16.dp))
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.show_detail_tab_about)) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.show_detail_tab_episodes)) },
                    )
                }
            }

            if (selectedTab == 0) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(stringResource(R.string.show_detail_about_section_title))
                        Spacer(Modifier.padding(top = 12.dp))
                        Text(uiState.overview, style = MaterialTheme.typography.bodyMedium)

                        uiState.trailerYoutubeKey?.let { key ->
                            Spacer(Modifier.padding(top = 16.dp))
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key")),
                                    )
                                },
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Text(
                                    stringResource(R.string.show_detail_trailer_button),
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }

                        if (uiState.nextEpisodeAirDate != null || uiState.averageEpisodeRuntimeMinutes != null) {
                            Spacer(Modifier.padding(top = 24.dp))
                            SectionHeader(stringResource(R.string.show_detail_diffusion_section_title))
                            Spacer(Modifier.padding(top = 12.dp))
                            uiState.nextEpisodeAirDate?.let {
                                Text(
                                    stringResource(R.string.show_detail_next_episode_air_format, formatAirDate(it)),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            uiState.averageEpisodeRuntimeMinutes?.let {
                                Text(
                                    stringResource(R.string.show_detail_episode_runtime_format, it),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (uiState.cast.isNotEmpty() || uiState.creatorNames != null) {
                            Spacer(Modifier.padding(top = 24.dp))
                            SectionHeader(stringResource(R.string.preview_cast_section_title))
                            Spacer(Modifier.padding(top = 12.dp))
                            uiState.creatorNames?.let {
                                Text(
                                    stringResource(R.string.show_detail_creators_format, it),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )
                            }
                            if (uiState.cast.isNotEmpty()) {
                                CastRow(uiState.cast, onPersonClick = onPersonClick)
                            }
                        }

                        Spacer(Modifier.padding(top = 24.dp))
                        SectionHeader(stringResource(R.string.preview_watch_providers_section_title))
                        Spacer(Modifier.padding(top = 12.dp))
                        WatchProvidersRow(
                            flatrate = uiState.watchProvidersFlatrate,
                            rent = uiState.watchProvidersRent,
                            buy = uiState.watchProvidersBuy,
                        )
                    }
                }
            } else {
                uiState.nextUnwatchedEpisode?.let { nextEpisode ->
                    item(key = "continue_watching") {
                        ContinueWatchingCard(
                            episode = nextEpisode,
                            onClick = { selectedSeasonNumber = nextEpisode.seasonNumber },
                        )
                    }
                }

                val effectiveSeasonNumber = selectedSeasonNumber
                    ?: uiState.nextUnwatchedEpisode?.seasonNumber
                    ?: uiState.seasons.firstOrNull()?.seasonNumber

                if (uiState.seasons.isNotEmpty() && effectiveSeasonNumber != null) {
                    item(key = "season_pills") {
                        SeasonPillTabs(
                            seasons = uiState.seasons.map { SeasonPillItem(it.seasonNumber, it.name) },
                            selectedSeasonNumber = effectiveSeasonNumber,
                            onSeasonSelected = { selectedSeasonNumber = it },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    val currentSeason = uiState.seasons.find { it.seasonNumber == effectiveSeasonNumber }
                    currentSeason?.let { season ->
                        item(key = "season_summary_${season.seasonNumber}") {
                            SeasonSummaryRow(
                                season = season,
                                onMarkAllWatched = { viewModel.onSeasonMarkAllWatched(season.seasonNumber) },
                            )
                        }
                        items(season.episodes, key = { "ep_${season.seasonNumber}_${it.episodeNumber}" }) { episode ->
                            val episodeKey = "${season.seasonNumber}_${episode.episodeNumber}"
                            val isEpisodeExpanded = expandedEpisodes.contains(episodeKey)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedEpisodes = if (isEpisodeExpanded) {
                                            expandedEpisodes - episodeKey
                                        } else {
                                            expandedEpisodes + episodeKey
                                        }
                                    },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                ) {
                                    WatchedToggleButton(
                                        checked = episode.watched,
                                        onCheckedChange = {
                                            viewModel.onEpisodeToggled(season.seasonNumber, episode.episodeNumber, it)
                                        },
                                    )
                                    AsyncImage(
                                        model = episode.stillUrl,
                                        contentDescription = episode.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(96.dp)
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        EpisodeCodeBadge(
                                            seasonNumber = season.seasonNumber,
                                            episodeNumber = episode.episodeNumber,
                                        )
                                        Text(episode.name, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                                if (isEpisodeExpanded) {
                                    Column(modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 12.dp)) {
                                        episode.voteAverage?.let { rating ->
                                            Text(
                                                stringResource(R.string.episode_rating_format, "%.1f".format(rating)),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.tertiary,
                                            )
                                        }
                                        Text(
                                            episode.overview?.takeIf { it.isNotBlank() }
                                                ?: stringResource(R.string.episode_no_overview),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBadge(rating: Float) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.height(16.dp),
            )
            Text(
                stringResource(R.string.show_detail_my_rating_format, rating.toString()),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

private fun formatAirDate(dateStr: String): String = try {
    val date = LocalDate.parse(dateStr)
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())
    date.format(formatter)
} catch (e: Exception) {
    dateStr
}

@Composable
private fun BroadcastStatusPill(status: ShowBroadcastStatus) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            status.displayLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun SeasonSummaryRow(season: SeasonUi, onMarkAllWatched: () -> Unit) {
    val watchedCount = season.episodes.count { it.watched }
    val allWatched = season.episodeCount > 0 && watchedCount == season.episodeCount

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.show_detail_season_progress_format, watchedCount, season.episodeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            WatchedToggleButton(
                checked = allWatched,
                onCheckedChange = { onMarkAllWatched() },
                modifier = Modifier.padding(start = 8.dp),
                contentDescription = stringResource(R.string.show_detail_mark_season_watched_content_description),
            )
        }
    }
}

@Composable
private fun ContinueWatchingCard(episode: NextEpisodeUi, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        SectionHeader(stringResource(R.string.show_detail_continue_watching_title))
        Spacer(Modifier.padding(top = 12.dp))
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                AsyncImage(
                    model = episode.stillUrl,
                    contentDescription = episode.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(96.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Text(
                    "${episode.seasonNumber}x${episode.episodeNumber} · ${episode.name}",
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
