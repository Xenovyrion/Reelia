package com.timeline.app.ui.tvtimeimport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvTimeImportScreen(onBack: () -> Unit, viewModel: TvTimeImportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onFileSelected)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tvtime_import_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = timeLineTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val state = uiState) {
                is TvTimeImportUiState.PickFile -> {
                    Text(stringResource(R.string.tvtime_import_pick_file_explanation), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { launcher.launch("*/*") }) {
                        Text(stringResource(R.string.tvtime_import_pick_file_button))
                    }
                }
                is TvTimeImportUiState.Parsing -> {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.tvtime_import_parsing), style = MaterialTheme.typography.bodyMedium)
                }
                is TvTimeImportUiState.ParseFailed -> {
                    Text(stringResource(R.string.tvtime_import_parse_failed), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = viewModel::reset) {
                        Text(stringResource(R.string.tvtime_import_try_again_button))
                    }
                }
                is TvTimeImportUiState.ReadyToImport -> {
                    Text(
                        stringResource(R.string.tvtime_import_ready_summary, state.data.shows.size, state.data.movies.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(stringResource(R.string.tvtime_import_ready_explanation), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = viewModel::startImport) {
                        Text(stringResource(R.string.tvtime_import_start_button))
                    }
                }
                is TvTimeImportUiState.Importing -> {
                    val progress = if (state.total > 0) state.done.toFloat() / state.total else 0f
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text(
                        stringResource(R.string.tvtime_import_progress_format, state.done, state.total),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is TvTimeImportUiState.Done -> {
                    Text(stringResource(R.string.tvtime_import_done_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(
                            R.string.tvtime_import_done_summary,
                            state.report.importedShowCount,
                            state.report.importedEpisodeCount,
                            state.report.importedMovieCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (state.report.unmatchedShowNames.isNotEmpty() || state.report.unmatchedMovieNames.isNotEmpty()) {
                        HorizontalDivider()
                        Text(stringResource(R.string.tvtime_import_unmatched_explanation), style = MaterialTheme.typography.bodySmall)
                        if (state.report.unmatchedShowNames.isNotEmpty()) {
                            Text(
                                stringResource(R.string.tvtime_import_unmatched_shows_title, state.report.unmatchedShowNames.size),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            state.report.unmatchedShowNames.forEach { name ->
                                Text("• $name", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (state.report.unmatchedMovieNames.isNotEmpty()) {
                            Text(
                                stringResource(R.string.tvtime_import_unmatched_movies_title, state.report.unmatchedMovieNames.size),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            state.report.unmatchedMovieNames.forEach { name ->
                                Text("• $name", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Button(onClick = onBack) {
                        Text(stringResource(R.string.tvtime_import_finish_button))
                    }
                }
            }
        }
    }
}
