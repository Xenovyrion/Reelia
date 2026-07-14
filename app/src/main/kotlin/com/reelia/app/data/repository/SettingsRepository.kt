package com.reelia.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.reelia.app.data.local.prefs.LanguagePreferenceStore
import com.reelia.app.data.local.prefs.TmdbApiKeyStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val SELECTED_PROVIDER_ID = stringPreferencesKey("selected_metadata_provider_id")
private const val DEFAULT_PROVIDER_ID = "tmdb"

@Singleton
class SettingsRepository @Inject constructor(
    private val apiKeyStore: TmdbApiKeyStore,
    private val languageStore: LanguagePreferenceStore,
    private val dataStore: DataStore<Preferences>,
) {
    val apiKey: Flow<String?> = apiKeyStore.apiKey

    suspend fun setApiKey(key: String) = apiKeyStore.setApiKey(key)

    val language: Flow<String> = languageStore.language

    suspend fun setLanguage(languageCode: String) = languageStore.setLanguage(languageCode)

    val selectedProviderId: Flow<String> =
        dataStore.data.map { it[SELECTED_PROVIDER_ID] ?: DEFAULT_PROVIDER_ID }

    suspend fun setSelectedProviderId(id: String) {
        dataStore.edit { it[SELECTED_PROVIDER_ID] = id }
    }
}
