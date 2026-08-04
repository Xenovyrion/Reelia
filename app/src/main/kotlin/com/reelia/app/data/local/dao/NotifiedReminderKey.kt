package com.reelia.app.data.local.dao

import com.reelia.app.domain.model.MediaType

/** Bulk-dedup projection for [com.reelia.app.data.local.entity.NotifiedReminderEntity] — backs
 * ReleaseReminderWorker's "already notified?" check without a per-row round trip. */
data class NotifiedReminderKey(
    val tmdbId: Int,
    val mediaType: MediaType,
    val offsetDays: Int,
    val targetDate: String,
)
