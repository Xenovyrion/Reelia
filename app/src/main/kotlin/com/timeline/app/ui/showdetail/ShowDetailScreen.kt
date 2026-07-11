package com.timeline.app.ui.showdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedSeasons by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(modifier = Modifier.padding(16.dp)) {
                    AsyncImage(
                        model = uiState.posterUrl,
                        contentDescription = uiState.title,
                        modifier = Modifier.height(180.dp),
                    )
                    Spacer(Modifier.padding(start = 12.dp))
                    Column {
                        Text(uiState.overview, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    viewModel.onEpisodeToggled(season.seasonNumber, episode.episodeNumber, !episode.watched)
                                },
                        ) {
                            Checkbox(
                                checked = episode.watched,
                                onCheckedChange = {
                                    viewModel.onEpisodeToggled(season.seasonNumber, episode.episodeNumber, it)
                                },
                            )
                            Text("${episode.episodeNumber}. ${episode.name}")
                        }
                    }
                }
            }
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
        Text(season.name, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.padding(top = 4.dp))
        LinearProgressIndicator(
            progress = { if (season.episodeCount == 0) 0f else watchedCount.toFloat() / season.episodeCount },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("$watchedCount / ${season.episodeCount} épisodes vus", style = MaterialTheme.typography.bodySmall)
    }
}
