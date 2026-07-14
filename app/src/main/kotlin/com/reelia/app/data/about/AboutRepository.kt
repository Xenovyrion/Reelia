package com.reelia.app.data.about

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val ABOUT_BASE_URL =
    "https://raw.githubusercontent.com/Xenovyrion/reelia-content/main/docs/about"

/**
 * Fetches the "what is this app" presentation Markdown from the repo on GitHub — deliberately
 * separate content from [com.reelia.app.data.guide.GuideRepository]'s user guide, which explains
 * how to use/configure the app rather than what it is.
 */
@Singleton
class AboutRepository @Inject constructor() {
    private val httpClient = OkHttpClient()

    suspend fun fetchAbout(language: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = if (language.startsWith("fr")) "fr.md" else "en.md"
            val request = Request.Builder().url("$ABOUT_BASE_URL/$fileName").build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                response.body?.string()?.takeIf { it.isNotBlank() } ?: error("Empty response")
            }
        }
    }
}
