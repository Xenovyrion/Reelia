package com.reelia.app.ui.profile

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.BuildConfig
import com.reelia.app.R
import com.reelia.app.data.local.prefs.LanguagePreferenceStore
import com.reelia.app.domain.model.displayLabel
import com.reelia.app.ui.auth.fetchGoogleIdToken
import com.reelia.app.ui.common.components.BarChart
import com.reelia.app.ui.common.components.CircularProgressRing
import com.reelia.app.ui.common.components.GenreProgressBar
import com.reelia.app.ui.common.components.GenreProgressItem
import com.reelia.app.ui.common.components.PasswordField
import com.reelia.app.ui.common.components.SectionHeader
import com.reelia.app.ui.common.components.StatCard
import com.reelia.app.ui.navigation.BottomNavScrollToTop
import com.reelia.app.ui.navigation.Routes
import com.reelia.app.ui.settings.LANGUAGE_DISPLAY_NAME_RES
import com.reelia.app.ui.theme.StatusFavorite
import com.reelia.app.ui.update.UpdateUiState
import com.reelia.app.ui.theme.StatusPlanned
import com.reelia.app.ui.theme.StatusWantToWatch
import com.reelia.app.ui.theme.StatusWatchingCompleted
import com.reelia.app.ui.theme.timeLineTopAppBarColors
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Schedule

private val StatusPalette = listOf(StatusWatchingCompleted, StatusWantToWatch, StatusPlanned, StatusFavorite)

private enum class ProfileSubTab { SETTINGS, STATS }

@Composable
private fun ProfileSubTab.label(): String = stringResource(
    when (this) {
        ProfileSubTab.SETTINGS -> R.string.profile_tab_settings
        ProfileSubTab.STATS -> R.string.profile_tab_stats
    },
)

@Composable
private fun StatsScope.label(): String = stringResource(
    when (this) {
        StatsScope.ALL -> R.string.stats_scope_all
        StatsScope.SERIES -> R.string.stats_scope_series
        StatsScope.FILMS -> R.string.stats_scope_films
    },
)

private fun formatCount(value: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

private fun formatHours(hours: Double): String = formatCount(hours.roundToInt())

@Composable
private fun formatWatchDuration(totalHours: Double): String {
    val totalWholeHours = (totalHours * 60).roundToInt() / 60
    val months = totalWholeHours / (24 * 30)
    val days = (totalWholeHours % (24 * 30)) / 24
    val hours = totalWholeHours % 24
    return stringResource(R.string.stats_duration_breakdown_format, months, days, hours)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onImportClick: () -> Unit = {},
    onReleaseNotesClick: () -> Unit = {},
    onGuideClick: () -> Unit = {},
    onStatsDetailClick: (filterType: String, filterId: String, filterLabel: String) -> Unit = { _, _, _ -> },
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rawStatsUiState by viewModel.statsUiState.collectAsStateWithLifecycle()
    val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()
    val deleteAccountUiState by viewModel.deleteAccountUiState.collectAsStateWithLifecycle()
    val resetLibraryUiState by viewModel.resetLibraryUiState.collectAsStateWithLifecycle()
    // Room's underlying flows (shows/movies/episodes/watch log) invalidate and re-query
    // independently rather than atomically, so combining their still-stale and already-cleared
    // values together during a reset can momentarily show nonsensical numbers — pin the stats to
    // a neutral zeroed state for that window instead of trusting the live combine output.
    val statsUiState = if (resetLibraryUiState.isResetting) {
        ProfileStatsUiState(scope = rawStatsUiState.scope)
    } else {
        rawStatsUiState
    }

    var selectedTab by remember { mutableStateOf(ProfileSubTab.SETTINGS) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val settingsScrollState = rememberScrollState()
    val statsScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        BottomNavScrollToTop.events.collect { route ->
            if (route == Routes.PROFILE) {
                when (selectedTab) {
                    ProfileSubTab.SETTINGS -> settingsScrollState.animateScrollTo(0)
                    ProfileSubTab.STATS -> statsScrollState.animateScrollTo(0)
                }
            }
        }
    }

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
                        viewModel.onDeleteAccountIntentConfirmed()
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

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text(stringResource(R.string.settings_reset_library_dialog_title)) },
            text = { Text(stringResource(R.string.settings_reset_library_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation = false
                        viewModel.onResetLibraryConfirmed()
                    },
                ) {
                    Text(stringResource(R.string.settings_reset_library_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text(stringResource(R.string.settings_reset_library_dialog_cancel))
                }
            },
        )
    }

    if (deleteAccountUiState.requiresRecentLogin) {
        ReauthDialog(
            isPasswordAccount = uiState.isPasswordAccount,
            isLoading = deleteAccountUiState.isReauthenticating,
            errorMessage = deleteAccountUiState.reauthErrorMessage,
            onPasswordConfirm = viewModel::onReauthenticateWithPassword,
            onGoogleConfirm = viewModel::onReauthenticateWithGoogle,
            onDismiss = viewModel::onReauthenticationDismissed,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_profile)) },
                colors = timeLineTopAppBarColors(),
                actions = {
                    IconButton(onClick = onGuideClick) {
                        Icon(
                            Icons.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.settings_guide_button),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                ProfileSubTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ProfileSubTab.entries.size),
                        label = { Text(tab.label()) },
                    )
                }
            }
            when (selectedTab) {
                ProfileSubTab.SETTINGS -> ProfileSettingsContent(
                    scrollState = settingsScrollState,
                    uiState = uiState,
                    updateUiState = updateUiState,
                    deleteAccountUiState = deleteAccountUiState,
                    resetLibraryUiState = resetLibraryUiState,
                    apiKeyInput = apiKeyInput,
                    onApiKeyInputChange = { apiKeyInput = it },
                    viewModel = viewModel,
                    onImportClick = onImportClick,
                    onReleaseNotesClick = onReleaseNotesClick,
                    onDeleteAccountClick = { showDeleteConfirmation = true },
                    onResetClick = { showResetConfirmation = true },
                )
                ProfileSubTab.STATS -> ProfileStatsContent(
                    scrollState = statsScrollState,
                    statsUiState = statsUiState,
                    viewModel = viewModel,
                    onStatsDetailClick = onStatsDetailClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSettingsContent(
    scrollState: ScrollState,
    uiState: ProfileUiState,
    updateUiState: UpdateUiState,
    deleteAccountUiState: DeleteAccountUiState,
    resetLibraryUiState: ResetLibraryUiState,
    apiKeyInput: String,
    onApiKeyInputChange: (String) -> Unit,
    viewModel: ProfileViewModel,
    onImportClick: () -> Unit,
    onReleaseNotesClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(onClick = viewModel::onSignOut, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_sign_out_button),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = onDeleteAccountClick,
                enabled = !deleteAccountUiState.isDeleting,
                modifier = Modifier.weight(1f),
            ) {
                if (deleteAccountUiState.isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp).size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(R.string.profile_delete_account_button),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        deleteAccountUiState.errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            OutlinedButton(onClick = onImportClick, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_tvtime_import_button),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = onResetClick,
                enabled = !resetLibraryUiState.isResetting,
                modifier = Modifier.weight(1f),
            ) {
                if (resetLibraryUiState.isResetting) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp).size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(R.string.settings_reset_library_button),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        resetLibraryUiState.errorMessage?.let {
            Text(
                stringResource(R.string.settings_reset_library_error, it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
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
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
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

        // Only TMDB is functional today — showing a one-item provider picker just invites
        // confusion. Reappears automatically once a second real provider is available.
        if (viewModel.providers.count { it.isAvailable } > 1) {
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
        }

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
            onValueChange = onApiKeyInputChange,
            label = { Text(stringResource(R.string.settings_api_key_field_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onApiKeySubmitted(apiKeyInput) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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
        Text(
            stringResource(R.string.settings_app_version_format, BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA.take(7)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        TextButton(
            onClick = onReleaseNotesClick,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp),
        ) {
            Text(stringResource(R.string.settings_release_notes_button))
        }

        if (BuildConfig.DEBUG) {
            val appCheckDebugToken by viewModel.appCheckDebugToken.collectAsStateWithLifecycle()
            val clipboardManager = LocalClipboardManager.current

            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.profile_appcheck_section_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.profile_appcheck_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            OutlinedButton(onClick = viewModel::onFetchAppCheckDebugTokenClicked) {
                Text(stringResource(R.string.profile_appcheck_fetch_button))
            }

            appCheckDebugToken?.let { token ->
                AlertDialog(
                    onDismissRequest = viewModel::onAppCheckDebugTokenDismissed,
                    title = { Text(stringResource(R.string.profile_appcheck_dialog_title)) },
                    text = { Text(token, style = MaterialTheme.typography.bodySmall) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(token))
                                viewModel.onAppCheckDebugTokenDismissed()
                            },
                        ) { Text(stringResource(R.string.profile_appcheck_copy_button)) }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onAppCheckDebugTokenDismissed) {
                            Text(stringResource(R.string.profile_appcheck_close_button))
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileStatsContent(
    scrollState: ScrollState,
    statsUiState: ProfileStatsUiState,
    viewModel: ProfileViewModel,
    onStatsDetailClick: (filterType: String, filterId: String, filterLabel: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
    ) {
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
                value = formatCount(statsUiState.totalWatchedCount),
                unitLabel = stringResource(R.string.stats_titles_unit_label),
                caption = stringResource(R.string.stats_titles_caption),
                accentColor = StatusWatchingCompleted,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                icon = Icons.Filled.Schedule,
                value = formatHours(statsUiState.totalHoursWatched),
                unitLabel = stringResource(R.string.stats_hours_unit_label),
                caption = stringResource(R.string.stats_hours_caption),
                accentColor = StatusPlanned,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                icon = Icons.Filled.CheckCircle,
                value = formatCount(statsUiState.completedCount),
                unitLabel = stringResource(R.string.stats_completed_unit_label),
                caption = stringResource(R.string.stats_completed_caption),
                accentColor = StatusFavorite,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Text(
            formatWatchDuration(statsUiState.totalHoursWatched),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Filled.Inventory2,
                value = formatCount(statsUiState.remainingCount),
                unitLabel = stringResource(R.string.stats_remaining_unit_label),
                caption = stringResource(R.string.stats_remaining_caption),
                accentColor = StatusWantToWatch,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                icon = Icons.Filled.HourglassBottom,
                value = formatHours(statsUiState.remainingHoursEstimate),
                unitLabel = stringResource(R.string.stats_hours_unit_label),
                caption = stringResource(R.string.stats_remaining_hours_caption),
                accentColor = StatusPlanned,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                value = String.format(Locale.getDefault(), "%.1f", statsUiState.averageHoursPerWeek),
                unitLabel = stringResource(R.string.stats_hours_unit_label),
                caption = stringResource(R.string.stats_weekly_average_caption),
                accentColor = StatusWatchingCompleted,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Card(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
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
            Column(modifier = Modifier.padding(top = 28.dp)) {
                SectionHeader(stringResource(R.string.stats_genre_section_title))
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    statsUiState.genreBreakdown.forEach { genre ->
                        GenreProgressBar(
                            item = genre,
                            onClick = { onStatsDetailClick("GENRE", genre.genreId.toString(), genre.name) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        )
                    }
                }
            }
        }

        if (statsUiState.showsAddedCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Filled.LiveTv,
                    value = statsUiState.showsAddedCount.toString(),
                    unitLabel = stringResource(R.string.stats_shows_added_unit_label),
                    caption = stringResource(R.string.stats_shows_added_caption),
                    accentColor = StatusWatchingCompleted,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatCard(
                    icon = Icons.Filled.Autorenew,
                    value = statsUiState.showsAiringCount.toString(),
                    unitLabel = stringResource(R.string.stats_shows_added_unit_label),
                    caption = stringResource(R.string.show_broadcast_status_returning),
                    accentColor = StatusPlanned,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            if (statsUiState.showsByBroadcastStatus.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    SectionHeader(stringResource(R.string.stats_broadcast_status_section_title))
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        statsUiState.showsByBroadcastStatus.forEachIndexed { index, stat ->
                            val statusLabel = stat.status.displayLabel()
                            GenreProgressBar(
                                item = GenreProgressItem(
                                    genreId = index,
                                    name = statusLabel,
                                    fraction = stat.fraction,
                                    color = StatusPalette[index % StatusPalette.size],
                                ),
                                onClick = { onStatsDetailClick("BROADCAST_STATUS", stat.status.name, statusLabel) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            )
                        }
                    }
                }
            }
            if (statsUiState.networkBreakdown.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    SectionHeader(stringResource(R.string.stats_network_section_title))
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        statsUiState.networkBreakdown.forEachIndexed { index, stat ->
                            GenreProgressBar(
                                item = GenreProgressItem(
                                    genreId = index,
                                    name = stat.name,
                                    fraction = stat.fraction,
                                    color = StatusPalette[index % StatusPalette.size],
                                ),
                                onClick = { onStatsDetailClick("NETWORK", stat.name, stat.name) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(top = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SectionHeader(stringResource(R.string.stats_weekly_section_title), modifier = Modifier.weight(1f))
                IconButton(onClick = viewModel::onWeeklyChartPrevious) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_chart_previous))
                }
                IconButton(onClick = viewModel::onWeeklyChartNext, enabled = statsUiState.weeklyOffset > 0) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_chart_next))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    stringResource(R.string.stats_weekly_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BarChart(entries = statsUiState.weeklyChart, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        }

        Column(modifier = Modifier.padding(top = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SectionHeader(stringResource(R.string.stats_monthly_section_title), modifier = Modifier.weight(1f))
                IconButton(onClick = viewModel::onMonthlyChartPrevious) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.stats_chart_previous))
                }
                IconButton(onClick = viewModel::onMonthlyChartNext, enabled = statsUiState.monthlyOffset > 0) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.stats_chart_next))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    stringResource(R.string.stats_monthly_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BarChart(entries = statsUiState.monthlyChart, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        }

        Column(modifier = Modifier.padding(top = 28.dp)) {
            SectionHeader(stringResource(R.string.stats_weekday_section_title))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    stringResource(R.string.stats_weekday_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BarChart(entries = statsUiState.weekdayChart, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** Firebase requires a "recent" sign-in before honoring account deletion — rather than force a
 * full sign-out/sign-in round trip, this re-proves identity in place: a password prompt for
 * email/password accounts, or a fresh Google credential for Google accounts. Deletion is
 * automatically retried by the ViewModel once reauthentication succeeds. */
@Composable
private fun ReauthDialog(
    isPasswordAccount: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onPasswordConfirm: (String) -> Unit,
    onGoogleConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_reauth_dialog_title)) },
        text = {
            Column {
                Text(
                    stringResource(
                        if (isPasswordAccount) R.string.profile_reauth_dialog_message_password else R.string.profile_reauth_dialog_message_google,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (isPasswordAccount) {
                    PasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.login_password_label),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                }
                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading && (!isPasswordAccount || password.isNotBlank()),
                onClick = {
                    if (isPasswordAccount) {
                        onPasswordConfirm(password)
                    } else {
                        coroutineScope.launch {
                            try {
                                onGoogleConfirm(fetchGoogleIdToken(context))
                            } catch (e: Exception) {
                                // Credential Manager itself failed (cancelled, no account, …) —
                                // there's no dedicated slot for this, so it's silently retryable.
                            }
                        }
                    }
                },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp).size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(
                            if (isPasswordAccount) R.string.profile_reauth_confirm_button else R.string.profile_reauth_google_button,
                        ),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_reauth_cancel_button))
            }
        },
    )
}
