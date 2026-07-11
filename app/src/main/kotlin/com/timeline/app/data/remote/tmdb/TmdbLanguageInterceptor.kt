package com.timeline.app.data.remote.tmdb

import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/** Injects the user's TMDB content-language (and derived region) into every request. */
class TmdbLanguageInterceptor @Inject constructor(
    private val languageStore: LanguagePreferenceStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val language = languageStore.currentLanguage
        val region = language.substringAfter('-', missingDelimiterValue = "")
        val originalUrl = chain.request().url
        val newUrlBuilder = originalUrl.newBuilder()
            .addQueryParameter("language", language)
        if (region.isNotEmpty()) {
            newUrlBuilder.addQueryParameter("region", region)
        }
        val newRequest = chain.request().newBuilder().url(newUrlBuilder.build()).build()
        return chain.proceed(newRequest)
    }
}
