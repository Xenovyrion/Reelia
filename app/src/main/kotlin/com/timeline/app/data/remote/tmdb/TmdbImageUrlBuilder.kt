package com.timeline.app.data.remote.tmdb

import com.timeline.app.data.local.prefs.TmdbConfigStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbImageUrlBuilder @Inject constructor(
    private val configStore: TmdbConfigStore,
    private val tmdbApi: TmdbApi,
) {
    private companion object {
        const val POSTER_SIZE = "w342"
        const val BACKDROP_SIZE = "w780"
        const val FALLBACK_BASE_URL = "https://image.tmdb.org/t/p/"
    }

    private suspend fun baseUrl(): String {
        configStore.getCachedImageBaseUrl()?.let { return it }
        return try {
            val config = tmdbApi.getConfiguration()
            configStore.setImageBaseUrl(config.images.secureBaseUrl)
            config.images.secureBaseUrl
        } catch (_: Exception) {
            FALLBACK_BASE_URL
        }
    }

    suspend fun posterUrl(posterPath: String?, size: String = POSTER_SIZE): String? {
        if (posterPath == null) return null
        return "${baseUrl()}$size$posterPath"
    }

    /** Full-bleed hero image for a show's detail screen. */
    suspend fun backdropUrl(backdropPath: String?): String? {
        if (backdropPath == null) return null
        return "${baseUrl()}$BACKDROP_SIZE$backdropPath"
    }
}
