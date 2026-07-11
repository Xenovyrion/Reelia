package com.timeline.app.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.ui.common.components.BackdropHeader
import com.timeline.app.ui.common.components.CastRow
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.WatchProvidersRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePreviewScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: MoviePreviewViewModel = hiltViewModel(),
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
            BackdropHeader(imageUrl = uiState.posterUrl, contentDescription = uiState.title, onBack = onBack)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(uiState.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val metadata = listOfNotNull(
                        uiState.releaseDate,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Text("%.1f/10".format(rating), modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.padding(top = 16.dp))
                Text(uiState.overview, style = MaterialTheme.typography.bodyMedium)

                if (uiState.cast.isNotEmpty()) {
                    Spacer(Modifier.padding(top = 24.dp))
                    SectionHeader(stringResource(R.string.preview_cast_section_title))
                    Spacer(Modifier.padding(top = 12.dp))
                    CastRow(uiState.cast)
                }

                Spacer(Modifier.padding(top = 24.dp))
                SectionHeader(stringResource(R.string.preview_watch_providers_section_title))
                Spacer(Modifier.padding(top = 12.dp))
                WatchProvidersRow(
                    flatrate = uiState.watchProvidersFlatrate,
                    rent = uiState.watchProvidersRent,
                    buy = uiState.watchProvidersBuy,
                )

                uiState.errorMessageRes?.let {
                    Text(stringResource(it), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(Modifier.padding(top = 24.dp))
                Button(
                    onClick = { viewModel.onAddClicked(onAdded) },
                    enabled = !uiState.isAdding && !uiState.added,
                ) {
                    Text(
                        if (uiState.added) {
                            stringResource(R.string.preview_added_button)
                        } else {
                            stringResource(R.string.preview_add_button)
                        },
                    )
                }
            }
        }
    }
}
