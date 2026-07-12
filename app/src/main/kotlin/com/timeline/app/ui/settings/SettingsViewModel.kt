package com.timeline.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.auth.AuthRepository
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.data.metadata.MetadataProvider
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.repository.SettingsRepository
import com.timeline.app.data.sync.FirestoreSyncRepository
import com.timeline.app.data.update.AppUpdateRepository
import com.timeline.app.ui.update.UpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String? = null,
    val language: String = LanguagePreferenceStore.FALLBACK_LANGUAGE,
    val selectedProviderId: String = "tmdb",
    val accountEmail: String? = null,
    val lastSyncedAt: Instant? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val appUpdateRepository: AppUpdateRepository,
    metadataProviderRegistry: MetadataProviderRegistry,
) : ViewModel() {

    val providers: List<MetadataProvider> = metadataProviderRegistry.providers

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.apiKey,
        settingsRepository.language,
        settingsRepository.selectedProviderId,
        authRepository.currentUser,
        firestoreSyncRepository.lastSyncedAt,
    ) { apiKey, language, providerId, user, lastSyncedAt ->
        SettingsUiState(
            apiKey = apiKey,
            language = language,
            selectedProviderId = providerId,
            accountEmail = user?.email,
            lastSyncedAt = lastSyncedAt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onSignOut() {
        authRepository.signOut()
    }

    private val saveEventChannel = Channel<Unit>(Channel.BUFFERED)
    val saveEvent: Flow<Unit> = saveEventChannel.receiveAsFlow()

    fun onApiKeySubmitted(key: String) {
        viewModelScope.launch {
            val trimmedKey = key.trim()
            settingsRepository.setApiKey(trimmedKey)
            saveEventChannel.send(Unit)
            // Push so the other device can import this key automatically instead of requiring
            // it to be re-typed after every reinstall.
            firestoreSyncRepository.pushApiKey(trimmedKey)
            // Retry sync hydration: on a fresh install, remote shows/movies can only be fetched
            // from TMDB once a key is set here, so any hydration that failed for lack of a key
            // needs a fresh listener snapshot to retry now that one exists.
            firestoreSyncRepository.startListening()
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(languageCode)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(LanguagePreferenceStore.uiLocaleTagFor(languageCode)),
            )
        }
    }

    fun onProviderSelected(providerId: String) {
        viewModelScope.launch { settingsRepository.setSelectedProviderId(providerId) }
    }

    // Manual "check for updates" flow — independent of the auto-check-once banner (see
    // UpdateViewModel), since this ViewModel is scoped to the Settings screen, not the app root.
    private val _updateUiState = MutableStateFlow(UpdateUiState())
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    fun onCheckForUpdateClicked() {
        viewModelScope.launch {
            _updateUiState.update { it.copy(isChecking = true, errorMessage = null) }
            val update = appUpdateRepository.checkForUpdate()
            _updateUiState.update { it.copy(isChecking = false, hasChecked = true, availableUpdate = update) }
        }
    }

    fun onUpdateDownloadClicked() {
        val update = _updateUiState.value.availableUpdate ?: return
        viewModelScope.launch {
            _updateUiState.update { it.copy(isDownloading = true, errorMessage = null) }
            try {
                val uri = appUpdateRepository.downloadUpdate(update)
                _updateUiState.update { it.copy(isDownloading = false, downloadedApkUri = uri) }
            } catch (e: Exception) {
                _updateUiState.update { it.copy(isDownloading = false, errorMessage = e.message) }
            }
        }
    }

    fun buildInstallIntent(apkUri: Uri): Intent = appUpdateRepository.buildInstallIntent(apkUri)

    fun onInstallLaunched() {
        _updateUiState.update { it.copy(downloadedApkUri = null) }
    }
}
