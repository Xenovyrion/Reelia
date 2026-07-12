package com.timeline.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.timeline.app.ui.theme.timeLineTopAppBarColors
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()
    var apiKeyInput by remember { mutableStateOf("") }
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

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }, colors = timeLineTopAppBarColors()) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
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

            Spacer(Modifier.height(24.dp))
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
            Button(onClick = viewModel::onSignOut) {
                Text(stringResource(R.string.settings_sign_out_button))
            }

            Spacer(Modifier.height(24.dp))
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
