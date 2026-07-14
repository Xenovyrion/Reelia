package com.reelia.app.data.local.dao

import com.reelia.app.domain.model.MediaType
import java.time.Instant

/** Most recent watch_log entry per tracked title — backs the library's "recently watched" sort. */
data class LastWatchedEntry(
    val mediaType: MediaType,
    val tmdbId: Int,
    val watchedAt: Instant,
)
