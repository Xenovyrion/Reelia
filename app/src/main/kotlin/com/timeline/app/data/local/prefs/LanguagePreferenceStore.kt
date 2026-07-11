package com.timeline.app.data.local.prefs

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
 * Reads/writes the TMDB content-language preference (e.g. "fr-FR"). Same in-memory-cache
 * pattern as [TmdbApiKeyStore] so [currentLanguage] can be read synchronously from the
 * OkHttp interceptor without blocking a network thread on DataStore I/O.
 */
@Singleton
class LanguagePreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var currentLanguage: String = DEFAULT_LANGUAGE
        private set

    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: DEFAULT_LANGUAGE }

    init {
        storeScope.launch {
            language.collect { currentLanguage = it }
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { it[LANGUAGE] = languageCode }
    }

    companion object {
        const val DEFAULT_LANGUAGE = "fr-FR"
        val SUPPORTED_LANGUAGES = listOf("fr-FR", "en-US", "en-GB", "es-ES", "de-DE", "it-IT", "ja-JP")
        private val LANGUAGE = stringPreferencesKey("tmdb_language")
    }
}
