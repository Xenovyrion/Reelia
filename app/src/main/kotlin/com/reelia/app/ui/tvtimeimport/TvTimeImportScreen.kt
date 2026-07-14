package com.reelia.app.ui.tvtimeimport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.R
import com.reelia.app.ui.common.openInExternalBrowser
import com.reelia.app.ui.common.components.BackButton
import com.reelia.app.ui.theme.timeLineTopAppBarColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvTimeImportScreen(onBack: () -> Unit, viewModel: TvTimeImportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onFileSelected)
    }
    val context = LocalContext.current
    val gdprUrl = stringResource(R.string.tvtime_import_gdpr_url)
    var showDetails by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tvtime_import_title)) },
                navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val state = uiState) {
                is TvTimeImportUiState.PickFile -> {
                    Text(
                        stringResource(R.string.tvtime_import_pick_file_step1),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = { openInExternalBrowser(context, gdprUrl) }) {
                        Text(stringResource(R.string.tvtime_import_open_gdpr_button))
                    }
                    Text(
                        stringResource(R.string.tvtime_import_pick_file_step2),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { launcher.launch("*/*") }) {
                        Text(stringResource(R.string.tvtime_import_pick_file_button))
                    }
                }
                is TvTimeImportUiState.Parsing -> {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.tvtime_import_parsing), style = MaterialTheme.typography.bodyMedium)
                }
                is TvTimeImportUiState.ParseFailed -> {
                    Text(
                        stringResource(R.string.tvtime_import_parse_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = viewModel::reset) {
                        Text(stringResource(R.string.tvtime_import_try_again_button))
                    }
                }
                is TvTimeImportUiState.ReadyToImport -> {
                    Text(
                        stringResource(R.string.tvtime_import_ready_summary, state.data.shows.size, state.data.movies.size),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.tvtime_import_ready_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = viewModel::startImport) {
                        Text(stringResource(R.string.tvtime_import_start_button))
                    }
                }
                is TvTimeImportUiState.Importing -> {
                    val targetProgress = if (state.total > 0) state.done.toFloat() / state.total else 0f
                    val animatedProgress by animateFloatAsState(targetValue = targetProgress, label = "tvtime_import_progress")
                    Text(
                        "${(animatedProgress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp)),
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Text(
                        stringResource(R.string.tvtime_import_progress_format, state.done, state.total),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is TvTimeImportUiState.ImportFailed -> {
                    Text(
                        stringResource(R.string.tvtime_import_failed, state.message.orEmpty()),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = viewModel::reset) {
                        Text(stringResource(R.string.tvtime_import_try_again_button))
                    }
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
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Text(
                            stringResource(
                                if (showDetails) R.string.tvtime_import_hide_details_button else R.string.tvtime_import_show_details_button,
                            ),
                        )
                    }
                    if (showDetails) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (state.report.importedShowNames.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    stringResource(R.string.tvtime_import_imported_shows_title, state.report.importedShowNames.size),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                state.report.importedShowNames.forEach { name ->
                                    Text("• $name", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (state.report.importedMovieNames.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    stringResource(R.string.tvtime_import_imported_movies_title, state.report.importedMovieNames.size),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                state.report.importedMovieNames.forEach { name ->
                                    Text("• $name", style = MaterialTheme.typography.bodySmall)
                                }
                            }
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
