package com.reelia.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reelia.app.domain.model.MediaType
import java.time.Instant
import java.util.UUID

/**
 * Append-only log of watch events. Kept separate from the `watched` flags on
 * [EpisodeEntity]/[TrackedMovieEntity] so historical stats survive later edits
 * (e.g. unmarking an episode, or the show's runtime metadata changing).
 *
 * [syncId] is a client-generated UUID used as the Firestore document id for cross-device
 * sync, and lets the remote listener dedup entries it already inserted locally. Entries
 * logged before this field existed keep the migration's blank default and are never
 * retroactively synced — only new entries going forward.
 */
@Entity(tableName = "watch_log")
data class WatchLogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val mediaType: MediaType,
    val tmdbId: Int,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val runtimeMinutes: Int,
    val watchedAt: Instant,
)
