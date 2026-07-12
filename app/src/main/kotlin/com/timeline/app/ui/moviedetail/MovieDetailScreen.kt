package com.timeline.app.ui.moviedetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.ui.common.components.BackdropHeader
import com.timeline.app.ui.common.components.CastRow
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.WatchProvidersRow
import com.timeline.app.ui.common.format.toYearOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            BackdropHeader(
                imageUrl = uiState.heroUrl,
                contentDescription = uiState.title,
                onBack = onBack,
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(uiState.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val metadata = listOfNotNull(
                        uiState.releaseDate.toYearOrNull(),
                        uiState.runtimeMinutes?.let { stringResource(R.string.movie_detail_runtime_minutes_format, it) },
                        uiState.genreNames.takeIf { it.isNotEmpty() }?.joinToString(", "),
                    ).joinToString(" • ")
                    Text(
                        metadata,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    uiState.voteAverage?.let { rating ->
                        RatingBadge(rating)
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        if (uiState.watched) {
                            stringResource(R.string.movie_detail_watched_badge)
                        } else {
                            stringResource(R.string.movie_detail_plan_to_watch_badge)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.padding(top = 16.dp))
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

                if (uiState.cast.isNotEmpty() || uiState.directorNames != null) {
                    Spacer(Modifier.padding(top = 24.dp))
                    SectionHeader(stringResource(R.string.preview_cast_section_title))
                    Spacer(Modifier.padding(top = 12.dp))
                    uiState.directorNames?.let {
                        Text(
                            stringResource(R.string.movie_detail_director_format, it),
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

                Spacer(Modifier.padding(top = 24.dp))
                Button(
                    onClick = { viewModel.onWatchedToggled(!uiState.watched) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        if (uiState.watched) {
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
