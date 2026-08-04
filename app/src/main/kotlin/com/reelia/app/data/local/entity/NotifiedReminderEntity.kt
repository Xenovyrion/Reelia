package com.reelia.app.data.local.entity

import androidx.room.Entity
import com.reelia.app.domain.model.MediaType
import java.time.Instant

/**
 * Dedup record for release-date reminders — one row per (title, offset, target date) that has
 * already fired, so [com.reelia.app.data.notifications.ReleaseReminderWorker]'s daily run doesn't
 * re-notify for the same upcoming release every day between now and release day. [targetDate] is
 * part of the key (not just tmdbId/mediaType/offsetDays) so a rescheduled release date correctly
 * gets a fresh reminder instead of being silently treated as already-notified.
 */
@Entity(tableName = "notified_reminders", primaryKeys = ["tmdbId", "mediaType", "offsetDays", "targetDate"])
data class NotifiedReminderEntity(
    val tmdbId: Int,
    val mediaType: MediaType,
    val offsetDays: Int,
    val targetDate: String,
    val notifiedAt: Instant,
)
