package com.timeline.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.data.metadata.MetadataProvider
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val language: String = LanguagePreferenceStore.DEFAULT_LANGUAGE,
    val selectedProviderId: String = "tmdb",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    metadataProviderRegistry: MetadataProviderRegistry,
) : ViewModel() {

    val providers: List<MetadataProvider> = metadataProviderRegistry.providers

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.apiKey,
        settingsRepository.language,
        settingsRepository.selectedProviderId,
    ) { apiKey, language, providerId ->
        SettingsUiState(apiKey = apiKey, language = language, selectedProviderId = providerId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    private val saveEventChannel = Channel<Unit>(Channel.BUFFERED)
    val saveEvent: Flow<Unit> = saveEventChannel.receiveAsFlow()

    fun onApiKeySubmitted(key: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(key.trim())
            saveEventChannel.send(Unit)
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch { settingsRepository.setLanguage(languageCode) }
    }

    fun onProviderSelected(providerId: String) {
        viewModelScope.launch { settingsRepository.setSelectedProviderId(providerId) }
    }
}
