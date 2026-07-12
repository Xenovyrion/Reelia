package com.timeline.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.ui.common.components.BarChart
import com.timeline.app.ui.common.components.CircularProgressRing
import com.timeline.app.ui.common.components.GenreProgressBar
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.StatCard
import com.timeline.app.ui.settings.LANGUAGE_DISPLAY_NAME_RES
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.theme.timeLineTopAppBarColors
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule

@Composable
private fun StatsScope.label(): String = stringResource(
    when (this) {
        StatsScope.ALL -> R.string.stats_scope_all
        StatsScope.SERIES -> R.string.stats_scope_series
        StatsScope.FILMS -> R.string.stats_scope_films
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onGenreClick: (Int) -> Unit = {}, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statsUiState by viewModel.statsUiState.collectAsStateWithLifecycle()
    val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()
    val deleteAccountUiState by viewModel.deleteAccountUiState.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.apiKey) {
        uiState.apiKey?.let { apiKeyInput = it }
    }
    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect {
            snackbarHostState.showSnackbar(context.getString(R.string.settings_save_confirmation))
        }
    }
    LaunchedEffect(updateUiState.downloadedApkUri) {
        updateUiState.downloadedApkUri?.let { uri ->
            context.startActivity(viewModel.buildInstallIntent(uri))
            viewModel.onInstallLaunched()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.profile_delete_account_dialog_title)) },
            text = { Text(stringResource(R.string.profile_delete_account_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.onDeleteAccountConfirmed()
                    },
                ) {
                    Text(stringResource(R.string.profile_delete_account_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.profile_delete_account_dialog_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_profile)) }, colors = timeLineTopAppBarColors()) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // --- Compte ---
            Text(stringResource(R.string.settings_account_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            uiState.accountEmail?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                uiState.lastSyncedAt?.let {
                    stringResource(R.string.settings_last_synced_format, DateFormat.getDateTimeInstance().format(Date.from(it)))
                } ?: stringResource(R.string.settings_last_synced_never),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::onSignOut) {
                    Text(stringResource(R.string.settings_sign_out_button))
                }
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    enabled = !deleteAccountUiState.isDeleting,
                ) {
                    if (deleteAccountUiState.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.profile_delete_account_button), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (deleteAccountUiState.requiresRecentLogin) {
                Text(
                    stringResource(R.string.profile_delete_account_recent_login_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                deleteAccountUiState.errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 24.dp))

            // --- Statistiques ---
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsScope.entries.forEachIndexed { index, scope ->
                    SegmentedButton(
                        selected = statsUiState.scope == scope,
                        onClick = { viewModel.onStatsScopeSelected(scope) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsScope.entries.size),
                        label = { Text(scope.label()) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Filled.CheckCircle,
                    value = statsUiState.totalWatchedCount.toString(),
                    unitLabel = stringResource(R.string.stats_titles_unit_label),
                    caption = stringResource(R.string.stats_titles_caption),
                    accentColor = StatusWatchingCompleted,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatCard(
                    icon = Icons.Filled.Schedule,
                    value = String.format(Locale.getDefault(), "%.1f", statsUiState.totalHoursWatched),
                    unitLabel = stringResource(R.string.stats_hours_unit_label),
                    caption = stringResource(R.string.stats_hours_caption),
                    accentColor = StatusPlanned,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatCard(
                    icon = Icons.Filled.CheckCircle,
                    value = statsUiState.completedCount.toString(),
                    unitLabel = stringResource(R.string.stats_completed_unit_label),
                    caption = stringResource(R.string.stats_completed_caption),
                    accentColor = StatusFavorite,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressRing(
                            progress = statsUiState.completedFraction,
                            size = 56.dp,
                            strokeWidth = 6.dp,
                            color = StatusWatchingCompleted,
                        )
                        Text(
                            "${(statsUiState.completedFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        stringResource(R.string.stats_completion_ring_caption),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }

            if (statsUiState.genreBreakdown.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    SectionHeader(stringResource(R.string.stats_genre_section_title))
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        statsUiState.genreBreakdown.forEach { genre ->
                            GenreProgressBar(
                                item = genre,
                                onClick = { onGenreClick(genre.genreId) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(stringResource(R.string.stats_weekly_section_title), modifier = Modifier.weight(1f))
                    IconButton(onClick = viewModel::onWeeklyChartPrevious) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_chart_previous))
                    }
                    IconButton(onClick = viewModel::onWeeklyChartNext, enabled = statsUiState.weeklyOffset > 0) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_chart_next))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        stringResource(R.string.stats_weekly_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = statsUiState.weeklyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(stringResource(R.string.stats_monthly_section_title), modifier = Modifier.weight(1f))
                    IconButton(onClick = viewModel::onMonthlyChartPrevious) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_chart_previous))
                    }
                    IconButton(onClick = viewModel::onMonthlyChartNext, enabled = statsUiState.monthlyOffset > 0) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_chart_next))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        stringResource(R.string.stats_monthly_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = statsUiState.monthlyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(top = 24.dp))

            // --- Préférences ---
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_language_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            var languageMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = languageMenuExpanded,
                onExpandedChange = { languageMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = LANGUAGE_DISPLAY_NAME_RES[uiState.language]?.let { stringResource(it) } ?: uiState.language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_language_field_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                    modifier = Modifier.exposedDropdownSize(),
                ) {
                    LanguagePreferenceStore.SUPPORTED_LANGUAGES.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(LANGUAGE_DISPLAY_NAME_RES[code]?.let { stringResource(it) } ?: code) },
                            onClick = {
                                viewModel.onLanguageSelected(code)
                                languageMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_provider_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            viewModel.providers.forEach { provider ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(
                        selected = uiState.selectedProviderId == provider.id,
                        onClick = { if (provider.isAvailable) viewModel.onProviderSelected(provider.id) },
                        enabled = provider.isAvailable,
                    )
                    Text(
                        provider.displayName,
                        color = if (provider.isAvailable) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            Text(
                stringResource(R.string.settings_provider_more_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_api_key_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_api_key_explanation),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text(stringResource(R.string.settings_api_key_field_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.onApiKeySubmitted(apiKeyInput) }) {
                Text(stringResource(R.string.settings_save_button))
            }

            // --- Mises à jour ---
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.settings_update_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when {
                updateUiState.isChecking -> Text(
                    stringResource(R.string.settings_update_checking),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                updateUiState.availableUpdate != null -> Text(
                    stringResource(R.string.settings_update_available),
                    style = MaterialTheme.typography.bodyMedium,
                )
                updateUiState.hasChecked -> Text(
                    stringResource(R.string.settings_update_up_to_date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row {
                if (updateUiState.availableUpdate != null) {
                    Button(onClick = viewModel::onUpdateDownloadClicked, enabled = !updateUiState.isDownloading) {
                        if (updateUiState.isDownloading) {
                            Text(stringResource(R.string.settings_update_checking))
                        } else {
                            Text(stringResource(R.string.update_action_download_install))
                        }
                    }
                } else {
                    Button(onClick = viewModel::onCheckForUpdateClicked, enabled = !updateUiState.isChecking) {
                        Text(stringResource(R.string.settings_update_check_button))
                    }
                }
            }
        }
    }
}
