package com.reelia.app.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Reads/writes the user's own TMDB API key. We keep an in-memory cache updated by a
 * background collector so [currentKey] can be read synchronously from the OkHttp
 * interceptor without blocking a network thread on DataStore I/O.
 */
@Singleton
class TmdbApiKeyStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var currentKey: String? = null
        private set

    val apiKey: Flow<String?> = dataStore.data.map { it[API_KEY] }

    init {
        storeScope.launch {
            apiKey.collect { currentKey = it }
        }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[API_KEY] = key }
    }

    private companion object {
        val API_KEY = stringPreferencesKey("tmdb_api_key")
    }
}
