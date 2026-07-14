package com.timeline.app.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Remembers the id of the last announcement (see docs/announcement.json) the user has already
 * seen/dismissed, so the same message doesn't reappear on every launch — only a new id does. */
@Singleton
class AnnouncementPreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val lastSeenId: Flow<String?> = dataStore.data.map { it[LAST_SEEN_ID] }

    suspend fun markSeen(id: String) {
        dataStore.edit { it[LAST_SEEN_ID] = id }
    }

    private companion object {
        val LAST_SEEN_ID = stringPreferencesKey("announcement_last_seen_id")
    }
}
