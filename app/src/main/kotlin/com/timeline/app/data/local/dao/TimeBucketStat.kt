package com.timeline.app.data.local.dao

/** One row of a weekly/monthly watch-time aggregation, e.g. bucket="2026-W15" or "2026-04". */
data class TimeBucketStat(
    val bucket: String,
    val totalMinutes: Int,
    val totalCount: Int,
)
