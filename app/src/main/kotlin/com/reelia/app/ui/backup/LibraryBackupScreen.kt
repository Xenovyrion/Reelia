package com.reelia.app.ui.backup

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.R
import com.reelia.app.ui.common.components.BackButton
import com.reelia.app.ui.theme.timeLineTopAppBarColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryBackupScreen(onBack: () -> Unit, viewModel: LibraryBackupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportFileName = stringResource(
        R.string.backup_export_file_name,
        DateTimeFormatter.ofPattern("yyyy-MM-dd").format(Instant.now().atZone(ZoneId.systemDefault())),
    )
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> viewModel.onExportTargetSelected(uri) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onFileSelected)
    }

    // Export status is transient — clear it a few seconds after showing so it doesn't linger
    // stale if the user comes back to this screen later.
    LaunchedEffect(uiState.exportMessage) {
        if (uiState.exportMessage != null) {
            delay(4000)
            viewModel.onExportMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_screen_title)) },
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
        ) {
            // --- Export ---
            Text(stringResource(R.string.backup_export_section_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.backup_export_explanation), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { exportLauncher.launch(exportFileName) },
                enabled = !uiState.isExporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp).size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.backup_export_button))
                }
            }
            uiState.exportMessage?.let { message ->
                Text(
                    when (message) {
                        ExportMessage.Success -> stringResource(R.string.backup_export_success)
                        is ExportMessage.Failure -> stringResource(R.string.backup_export_failed, message.message.orEmpty())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message is ExportMessage.Failure) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalDivider()

            // --- Restore ---
            Text(stringResource(R.string.backup_import_section_title), style = MaterialTheme.typography.titleMedium)
            when (val phase = uiState.importPhase) {
                is ImportPhase.PickFile -> {
                    Text(stringResource(R.string.backup_import_explanation), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { importLauncher.launch("application/json") }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.backup_import_pick_file_button))
                    }
                }
                is ImportPhase.Parsing -> {
                    BackupCenteredContent {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.backup_import_parsing), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is ImportPhase.ParseFailed -> {
                    BackupCenteredContent {
                        Text(
                            stringResource(
                                if (phase.isUnsupportedVersion) {
                                    R.string.backup_import_unsupported_version
                                } else {
                                    R.string.backup_import_parse_failed
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = viewModel::resetImport) {
                            Text(stringResource(R.string.backup_import_try_again_button))
                        }
                    }
                }
                is ImportPhase.ReadyToImport -> {
                    val exportedAtText = DateFormat.getDateFormat(context).format(Date(phase.backup.exportedAt))
                    BackupCenteredContent {
                        Text(
                            stringResource(
                                R.string.backup_import_ready_summary,
                                phase.backup.shows.size,
                                phase.backup.movies.size,
                                exportedAtText,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            stringResource(R.string.backup_import_ready_explanation),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = viewModel::startImport) {
                            Text(stringResource(R.string.backup_import_start_button))
                        }
                    }
                }
                is ImportPhase.Importing -> {
                    BackupCenteredContent {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.backup_import_importing), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is ImportPhase.ImportFailed -> {
                    BackupCenteredContent {
                        Text(
                            stringResource(R.string.backup_import_failed, phase.message.orEmpty()),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = viewModel::resetImport) {
                            Text(stringResource(R.string.backup_import_try_again_button))
                        }
                    }
                }
                is ImportPhase.Done -> {
                    BackupCenteredContent {
                        Text(stringResource(R.string.backup_import_done_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(
                                R.string.backup_import_done_summary,
                                phase.report.showCount,
                                phase.report.episodeCount,
                                phase.report.movieCount,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = viewModel::resetImport) {
                            Text(stringResource(R.string.backup_import_finish_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupCenteredContent(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
