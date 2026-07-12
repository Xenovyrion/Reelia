package com.timeline.app.data.local.entity

import androidx.room.Entity
import com.timeline.app.domain.model.MediaType
import java.time.Instant

/**
 * Marks a show/movie as having a locally-changed field (currently just `isFavorite`)
 * that still needs to be pushed to Firestore. Drained and cleared by
 * FirestoreSyncRepository.pushPendingChanges().
 */
@Entity(tableName = "sync_outbox", primaryKeys = ["tmdbId", "mediaType"])
data class SyncOutboxEntity(
    val tmdbId: Int,
    val mediaType: MediaType,
    val updatedAt: Instant,
)
