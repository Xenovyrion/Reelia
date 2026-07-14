package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.domain.model.MediaType

@Dao
interface SyncOutboxDao {
    @Upsert
    suspend fun markPending(entry: SyncOutboxEntity)

    @Query("SELECT * FROM sync_outbox")
    suspend fun getAllPending(): List<SyncOutboxEntity>

    @Query("DELETE FROM sync_outbox WHERE tmdbId = :tmdbId AND mediaType = :mediaType")
    suspend fun clearPending(tmdbId: Int, mediaType: MediaType)
}
