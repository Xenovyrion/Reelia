package com.timeline.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import java.time.Instant

@Entity(
    tableName = "episodes",
    primaryKeys = ["showId", "seasonNumber", "episodeNumber"],
    foreignKeys = [
        ForeignKey(
            entity = TrackedShowEntity::class,
            parentColumns = ["tmdbId"],
            childColumns = ["showId"],
            onDelete = CASCADE,
        ),
    ],
)
data class EpisodeEntity(
    val showId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val tmdbEpisodeId: Int,
    val name: String,
    val overview: String? = null,
    val voteAverage: Float? = null,
    val airDate: String?,
    val runtimeMinutes: Int?,
    val stillPath: String? = null,
    val watched: Boolean = false,
    val watchedAt: Instant? = null,
)
