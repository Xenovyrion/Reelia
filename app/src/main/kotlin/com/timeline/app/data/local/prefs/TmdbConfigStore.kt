package com.timeline.app.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Caches the TMDB `/configuration` image base URL so we don't refetch it on every launch. */
@Singleton
class TmdbConfigStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val imageBaseUrl: Flow<String?> = dataStore.data.map { it[IMAGE_BASE_URL] }

    suspend fun getCachedImageBaseUrl(): String? = imageBaseUrl.first()

    suspend fun setImageBaseUrl(url: String) {
        dataStore.edit { it[IMAGE_BASE_URL] = url }
    }

    private companion object {
        val IMAGE_BASE_URL = stringPreferencesKey("tmdb_image_base_url")
    }
}
