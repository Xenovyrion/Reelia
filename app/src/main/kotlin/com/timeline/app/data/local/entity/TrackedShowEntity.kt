package com.timeline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timeline.app.domain.model.WatchStatus
import java.time.Instant

@Entity(tableName = "tracked_shows")
data class TrackedShowEntity(
    @PrimaryKey val tmdbId: Int,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val firstAirDate: String?,
    val status: WatchStatus,
    val userRating: Float?,
    val addedAt: Instant,
    val lastUpdatedAt: Instant,
    val numberOfSeasons: Int,
    val nextEpisodeToAirDate: String?,
    val nextEpisodeToAirName: String?,
    val nextEpisodeSeasonNumber: Int?,
    val nextEpisodeNumber: Int?,
    val averageEpisodeRuntimeMinutes: Int?,
    val networkNames: String? = null,
    val broadcastStatus: String? = null,
    val lastAirDate: String? = null,
    val creatorNames: String? = null,
    val isFavorite: Boolean = false,
    val lastModifiedAt: Instant = Instant.now(),
)
