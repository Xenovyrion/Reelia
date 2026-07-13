package com.timeline.app.data.releasenotes

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val RELEASE_NOTES_BASE_URL =
    "https://raw.githubusercontent.com/Xenovyrion/TimeLine/main/docs/release-notes"

/**
 * Fetches the release notes Markdown straight from the repo on GitHub — rather than bundling
 * them in the APK — so new entries show up for every install without needing an app update
 * (matching FR/EN files, same source of truth as the code itself).
 */
@Singleton
class ReleaseNotesRepository @Inject constructor() {
    private val httpClient = OkHttpClient()

    suspend fun fetchReleaseNotes(language: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = if (language == "fr") "fr.md" else "en.md"
            val request = Request.Builder().url("$RELEASE_NOTES_BASE_URL/$fileName").build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                response.body?.string()?.takeIf { it.isNotBlank() } ?: error("Empty response")
            }
        }
    }
}
