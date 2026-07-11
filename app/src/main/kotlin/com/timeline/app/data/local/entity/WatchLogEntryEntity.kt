package com.timeline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timeline.app.domain.model.MediaType
import java.time.Instant

/**
 * Append-only log of watch events. Kept separate from the `watched` flags on
 * [EpisodeEntity]/[TrackedMovieEntity] so historical stats survive later edits
 * (e.g. unmarking an episode, or the show's runtime metadata changing).
 */
@Entity(tableName = "watch_log")
data class WatchLogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaType: MediaType,
    val tmdbId: Int,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val runtimeMinutes: Int,
    val watchedAt: Instant,
)
