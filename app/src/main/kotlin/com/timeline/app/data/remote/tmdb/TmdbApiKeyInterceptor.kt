package com.timeline.app.data.remote.tmdb

import com.timeline.app.data.local.prefs.TmdbApiKeyStore
import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/** Thrown when a TMDB call is attempted before the user has entered their own API key. */
class MissingTmdbApiKeyException : IOException("No TMDB API key configured")

class TmdbApiKeyInterceptor @Inject constructor(
    private val apiKeyStore: TmdbApiKeyStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyStore.currentKey ?: throw MissingTmdbApiKeyException()
        val originalUrl = chain.request().url
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val newRequest = chain.request().newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
