package com.reelia.app.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Release-date reminder settings — off by default (never auto-enabled; the user has to opt in
 * from Profil > Paramètres, which is also where the POST_NOTIFICATIONS runtime permission gets
 * requested). See [ReleaseReminderWorker][com.reelia.app.data.notifications.ReleaseReminderWorker]
 * for how these are consumed.
 */
@Singleton
class NotificationPreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: false }

    val enabledOffsets: Flow<Set<Int>> = dataStore.data.map { prefs ->
        prefs[ENABLED_OFFSETS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: DEFAULT_OFFSETS
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setEnabledOffsets(offsets: Set<Int>) {
        dataStore.edit { it[ENABLED_OFFSETS] = offsets.map(Int::toString).toSet() }
    }

    companion object {
        /** Fixed checklist shown in Settings — J-30 down to release day, in that display order. */
        val AVAILABLE_OFFSETS = listOf(30, 14, 8, 1, 0)
        val DEFAULT_OFFSETS = setOf(8, 0)

        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val ENABLED_OFFSETS = stringSetPreferencesKey("notification_offsets")
    }
}
