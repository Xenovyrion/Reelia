package com.timeline.app.ui.settings

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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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
}
