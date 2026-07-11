package com.timeline.app.data.repository

import com.timeline.app.data.local.prefs.TmdbApiKeyStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SettingsRepository @Inject constructor(
    private val apiKeyStore: TmdbApiKeyStore,
) {
    val apiKey: Flow<String?> = apiKeyStore.apiKey

    suspend fun setApiKey(key: String) = apiKeyStore.setApiKey(key)
}
