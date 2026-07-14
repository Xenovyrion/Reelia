package com.reelia.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant

@Entity(tableName = "tracked_movies")
data class TrackedMovieEntity(
    @PrimaryKey val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val status: WatchStatus,
    val userRating: Float?,
    val addedAt: Instant,
    val watched: Boolean = false,
    val watchedAt: Instant? = null,
    val isFavorite: Boolean = false,
    val lastModifiedAt: Instant = Instant.now(),
)
