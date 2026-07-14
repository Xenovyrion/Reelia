package com.timeline.app.data.guide

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val GUIDE_BASE_URL =
    "https://raw.githubusercontent.com/Xenovyrion/reelia-content/main/docs/guide"

/**
 * Fetches the user guide Markdown straight from the repo on GitHub, same reasoning as
 * [com.timeline.app.data.releasenotes.ReleaseNotesRepository] — so edits to the guide show up
 * for every install without needing an app update.
 */
@Singleton
class GuideRepository @Inject constructor() {
    private val httpClient = OkHttpClient()

    suspend fun fetchGuide(language: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            // language is e.g. "fr-FR"/"en-US" (see LanguagePreferenceStore) — only the leading
            // language part matters here.
            val fileName = if (language.startsWith("fr")) "fr.md" else "en.md"
            val request = Request.Builder().url("$GUIDE_BASE_URL/$fileName").build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                response.body?.string()?.takeIf { it.isNotBlank() } ?: error("Empty response")
            }
        }
    }
}
