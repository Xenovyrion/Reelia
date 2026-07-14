package com.reelia.app.data.announcement

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private const val ANNOUNCEMENT_URL =
    "https://raw.githubusercontent.com/Xenovyrion/reelia-content/main/docs/announcement.json"

/**
 * Fetches docs/announcement.json straight from GitHub, same "no app update needed" approach as
 * [com.reelia.app.data.releasenotes.ReleaseNotesRepository] — editing that file on GitHub is
 * enough to broadcast a message to every install.
 */
@Singleton
class AnnouncementRepository @Inject constructor() {
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /** Returns null if there's nothing to announce (fetch/parse failure, or an empty/missing
     * message for [language]) — callers don't need to distinguish "no network" from "no news". */
    suspend fun fetchAnnouncement(language: String): Announcement? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(ANNOUNCEMENT_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val dto = json.decodeFromString<AnnouncementDto>(body)
                if (dto.id.isBlank()) return@withContext null
                // language is e.g. "fr-FR"/"en-US" (see LanguagePreferenceStore) — only the
                // leading language part decides which entry to use.
                val languageKey = if (language.startsWith("fr")) "fr" else "en"
                val text = dto.message[languageKey]?.takeIf { it.isNotBlank() } ?: return@withContext null
                Announcement(id = dto.id, important = dto.important, message = text)
            }
        } catch (e: Exception) {
            null
        }
    }
}
