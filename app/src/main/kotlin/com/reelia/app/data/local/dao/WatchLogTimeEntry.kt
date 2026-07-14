package com.reelia.app.data.local.dao

import java.time.Instant

/** Raw projection used to bucket watch-log entries into weeks/months in Kotlin (see
 * StatsRepository) rather than in SQL, so buckets with zero entries can still be backfilled. */
data class WatchLogTimeEntry(
    val watchedAt: Instant,
    val runtimeMinutes: Int,
)
