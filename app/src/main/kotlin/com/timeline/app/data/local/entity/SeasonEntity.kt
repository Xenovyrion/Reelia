package com.timeline.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "seasons",
    primaryKeys = ["showId", "seasonNumber"],
    foreignKeys = [
        ForeignKey(
            entity = TrackedShowEntity::class,
            parentColumns = ["tmdbId"],
            childColumns = ["showId"],
            onDelete = CASCADE,
        ),
    ],
)
data class SeasonEntity(
    val showId: Int,
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val posterPath: String?,
    val airDate: String?,
)
