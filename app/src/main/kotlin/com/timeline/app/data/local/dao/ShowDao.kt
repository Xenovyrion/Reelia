package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.timeline.app.data.local.entity.ShowWithDetails
import com.timeline.app.data.local.entity.TrackedShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {
    @Query("SELECT * FROM tracked_shows ORDER BY addedAt DESC")
    fun getAllShows(): Flow<List<TrackedShowEntity>>

    @Transaction
    @Query("SELECT * FROM tracked_shows WHERE tmdbId = :showId")
    fun getShowWithDetails(showId: Int): Flow<ShowWithDetails?>

    @Upsert
    suspend fun upsertShow(show: TrackedShowEntity)

    @Query("UPDATE tracked_shows SET isFavorite = :isFavorite WHERE tmdbId = :showId")
    suspend fun setShowFavorite(showId: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteShow(show: TrackedShowEntity)
}
