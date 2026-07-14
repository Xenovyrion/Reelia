package com.reelia.app.data.local.dao

/** One row of a genre watch-time breakdown, e.g. genreName="Science-fiction", totalMinutes=840. */
data class GenreStat(
    val genreId: Int,
    val genreName: String,
    val totalMinutes: Int,
)
