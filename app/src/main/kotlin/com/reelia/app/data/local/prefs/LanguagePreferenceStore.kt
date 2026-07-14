package com.reelia.app.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.Locale
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
 *
 * The default (used only while no explicit choice has been persisted yet) is computed once
 * from the phone's system locale rather than hardcoded, so a fresh install starts in
 * whichever of [SUPPORTED_LANGUAGES] matches the device — falling back to [FALLBACK_LANGUAGE]
 * if the device's language isn't one of them. This is a one-time default, not recomputed on
 * every launch, so an explicit user choice is never silently overridden later.
 */
@Singleton
class LanguagePreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val defaultLanguage: String = systemDefaultLanguage()

    @Volatile
    var currentLanguage: String = defaultLanguage
        private set

    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: defaultLanguage }

    init {
        storeScope.launch {
            language.collect { currentLanguage = it }
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { it[LANGUAGE] = languageCode }
    }

    private fun systemDefaultLanguage(): String {
        val systemLanguage = Locale.getDefault().language
        return SUPPORTED_LANGUAGES.firstOrNull { it.substringBefore("-") == systemLanguage } ?: FALLBACK_LANGUAGE
    }

    companion object {
        const val FALLBACK_LANGUAGE = "en-US"
        // Only French and English UI text actually exists (see uiLocaleTagFor) — other TMDB
        // content languages were listed here but never had a real translated UI to back them.
        val SUPPORTED_LANGUAGES = listOf("fr-FR", "en-US")

        /** The app's own UI text is only translated for French and English — everything else falls
         * back to English resources automatically via normal Android resource resolution. This maps
         * a TMDB content-language code to the UI locale tag to request from AppCompat. */
        fun uiLocaleTagFor(languageCode: String): String = if (languageCode.startsWith("fr")) "fr" else "en"

        private val LANGUAGE = stringPreferencesKey("tmdb_language")
    }
}
