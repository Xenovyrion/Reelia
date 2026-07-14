package com.reelia.app.data.local.dao

/** Lightweight projection used to text-search episode titles across the whole library without
 * loading every episode's full row (overview, still image path, etc.). */
data class EpisodeNameRow(
    val showId: Int,
    val name: String,
)
