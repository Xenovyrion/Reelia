package com.timeline.app.ui.showdetail

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.displayLabel
import com.timeline.app.ui.common.components.BackdropHeader
import com.timeline.app.ui.common.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedSeasons by remember { mutableStateOf(setOf<Int>()) }
    var expandedEpisodes by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold { padding ->
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
                    Text(uiState.title, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.padding(top = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.season_count_format, uiState.seasonCount) +
                                " • ${uiState.status.displayLabel()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        uiState.userRating?.let { rating ->
                            RatingBadge(rating)
                        }
                    }
                    Spacer(Modifier.padding(top = 12.dp))
                    val progress = if (uiState.totalEpisodeCount == 0) {
                        0f
                    } else {
                        uiState.watchedEpisodeCount.toFloat() / uiState.totalEpisodeCount
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                    }
                }
            } else {
                uiState.seasons.forEach { season ->
                    val isExpanded = expandedSeasons.contains(season.seasonNumber)
                    item(key = "season_header_${season.seasonNumber}") {
                        SeasonHeader(
                            season = season,
                            onToggle = {
                                expandedSeasons = if (isExpanded) {
                                    expandedSeasons - season.seasonNumber
                                } else {
                                    viewModel.onSeasonExpanded(season.seasonNumber)
                                    expandedSeasons + season.seasonNumber
                                }
                            },
                        )
                    }
                    if (isExpanded) {
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
                                    Checkbox(
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
                                    Text(
                                        "${episode.episodeNumber}. ${episode.name}",
                                        modifier = Modifier.padding(start = 12.dp),
                                    )
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

@Composable
private fun SeasonHeader(season: SeasonUi, onToggle: () -> Unit) {
    val watchedCount = season.episodes.count { it.watched }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onToggle),
    ) {
        SectionHeader(season.name)
        Spacer(Modifier.padding(top = 8.dp))
        LinearProgressIndicator(
            progress = { if (season.episodeCount == 0) 0f else watchedCount.toFloat() / season.episodeCount },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.show_detail_season_progress_format, watchedCount, season.episodeCount),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
