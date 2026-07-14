package com.reelia.app.ui.showdetail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.reelia.app.R
import com.reelia.app.domain.model.ShowBroadcastStatus
import com.reelia.app.domain.model.displayLabel
import com.reelia.app.ui.common.components.BackdropHeader
import com.reelia.app.ui.common.components.CastRow
import com.reelia.app.ui.common.components.localizedCrewJobLabel
import com.reelia.app.ui.common.components.EpisodeCodeBadge
import com.reelia.app.ui.common.components.SeasonPillItem
import com.reelia.app.ui.common.components.SeasonPillTabs
import com.reelia.app.ui.common.components.SectionHeader
import com.reelia.app.ui.common.components.WatchProvidersRow
import com.reelia.app.ui.common.components.WatchedToggleButton
import com.reelia.app.ui.common.format.toFormattedDateOrNull
import com.reelia.app.ui.theme.StatusFavorite
import com.reelia.app.ui.theme.StatusWatchingCompleted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit,
    viewModel: ShowDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSeasonNumber by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedEpisodeKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.removedEvent.collect { onBack() }
    }

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
                    onDelete = { showRemoveConfirmation = true },
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
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                        }
                    }
                }

                if (uiState.nextEpisodeAirDate != null || uiState.averageEpisodeRuntimeMinutes != null) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.cast.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader(stringResource(R.string.preview_cast_section_title))
                                Spacer(Modifier.padding(top = 12.dp))
                                CastRow(uiState.cast, onPersonClick = onPersonClick)
                            }
                        }
                    }
                }

                if (uiState.crew.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionHeader(stringResource(R.string.preview_crew_section_title))
                                Spacer(Modifier.padding(top = 12.dp))
                                CastRow(
                                    uiState.crew.map { it.copy(character = localizedCrewJobLabel(it.character)) },
                                    onPersonClick = onPersonClick,
                                )
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader(stringResource(R.string.preview_watch_providers_section_title))
                            Spacer(Modifier.padding(top = 12.dp))
                            WatchProvidersRow(
                                flatrate = uiState.watchProvidersFlatrate,
                                rent = uiState.watchProvidersRent,
                                buy = uiState.watchProvidersBuy,
                            )
                        }
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
                                onMarkAllWatched = { watched -> viewModel.onSeasonMarkAllWatched(season.seasonNumber, watched) },
                            )
                        }
                        items(season.episodes, key = { "ep_${season.seasonNumber}_${it.episodeNumber}" }) { episode ->
                            EpisodeRow(
                                seasonNumber = season.seasonNumber,
                                episode = episode,
                                onClick = { selectedEpisodeKey = season.seasonNumber to episode.episodeNumber },
                                onWatchedChange = { watched, fillGaps ->
                                    viewModel.onEpisodeToggled(season.seasonNumber, episode.episodeNumber, watched, fillGaps)
                                },
                            )
                        }
                    }
                }
            }
        }

        selectedEpisodeKey?.let { (seasonNumber, episodeNumber) ->
            val currentEpisode = uiState.seasons
                .find { it.seasonNumber == seasonNumber }
                ?.episodes
                ?.find { it.episodeNumber == episodeNumber }
            if (currentEpisode != null) {
                EpisodeDetailSheet(
                    seasonNumber = seasonNumber,
                    episode = currentEpisode,
                    onWatchedToggled = { watched, fillGaps ->
                        viewModel.onEpisodeToggled(seasonNumber, episodeNumber, watched, fillGaps)
                    },
                    onDismiss = { selectedEpisodeKey = null },
                )
            }
        }
    }

    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text(stringResource(R.string.detail_remove_dialog_title, uiState.title)) },
            text = { Text(stringResource(R.string.detail_remove_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirmation = false
                        viewModel.onRemoveConfirmed()
                    },
                ) {
                    Text(stringResource(R.string.detail_remove_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) {
                    Text(stringResource(R.string.detail_remove_dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun EpisodeRow(
    seasonNumber: Int,
    episode: EpisodeUi,
    onClick: () -> Unit,
    onWatchedChange: (watched: Boolean, fillGaps: Boolean) -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (episode.watched) {
                StatusWatchingCompleted.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
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
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                EpisodeCodeBadge(seasonNumber = seasonNumber, episodeNumber = episode.episodeNumber)
                Text(episode.name, modifier = Modifier.padding(top = 4.dp))
            }
            WatchedToggleButton(
                checked = episode.watched,
                onCheckedChange = { checked -> onWatchedChange(checked, checked) },
                onLongPress = { onWatchedChange(!episode.watched, false) },
                size = 32.dp,
                modifier = Modifier.padding(start = 8.dp),
                contentDescription = stringResource(R.string.show_detail_mark_episode_watched_content_description),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeDetailSheet(
    seasonNumber: Int,
    episode: EpisodeUi,
    onWatchedToggled: (watched: Boolean, fillGaps: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            AsyncImage(
                model = episode.stillUrl,
                contentDescription = episode.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Column(modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp)) {
                EpisodeCodeBadge(seasonNumber = seasonNumber, episodeNumber = episode.episodeNumber)
                Text(
                    episode.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    episode.airDate?.let {
                        Text(
                            formatAirDate(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    episode.voteAverage?.let { rating ->
                        Text(
                            stringResource(R.string.episode_rating_format, "%.1f".format(rating)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
                Text(
                    episode.overview?.takeIf { it.isNotBlank() } ?: stringResource(R.string.episode_no_overview),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Button(
                    onClick = {
                        val newWatched = !episode.watched
                        onWatchedToggled(newWatched, newWatched)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    Text(
                        if (episode.watched) {
                            stringResource(R.string.movie_detail_watched_button)
                        } else {
                            stringResource(R.string.movie_detail_mark_watched_button)
                        },
                    )
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

private fun formatAirDate(dateStr: String): String = dateStr.toFormattedDateOrNull(includeWeekday = true) ?: dateStr

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
private fun SeasonSummaryRow(season: SeasonUi, onMarkAllWatched: (Boolean) -> Unit) {
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
                onCheckedChange = onMarkAllWatched,
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
