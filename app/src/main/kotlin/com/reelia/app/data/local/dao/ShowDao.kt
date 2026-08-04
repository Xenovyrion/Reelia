package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.reelia.app.data.local.entity.ShowWithDetails
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {
    @Query("SELECT * FROM tracked_shows ORDER BY addedAt DESC")
    fun getAllShows(): Flow<List<TrackedShowEntity>>

    @Query("SELECT * FROM tracked_shows WHERE tmdbId = :showId")
    suspend fun getShowOnce(showId: Int): TrackedShowEntity?

    /** Shows still actively being tracked (not completed/dropped) whose next-episode date is
     * unknown or already in the past — candidates for a fresh TMDB fetch before computing release
     * reminders, since `nextEpisodeToAirDate` is otherwise never refreshed after a show is added
     * (see ReleaseReminderWorker). [today] is an ISO "yyyy-MM-dd" string, matching TMDB's format. */
    @Query(
        """
        SELECT * FROM tracked_shows
        WHERE status IN ('WATCHING', 'PLAN_TO_WATCH', 'ON_HOLD')
        AND (nextEpisodeToAirDate IS NULL OR nextEpisodeToAirDate < :today)
        """,
    )
    suspend fun getShowsNeedingReleaseRefresh(today: String): List<TrackedShowEntity>

    @Transaction
    @Query("SELECT * FROM tracked_shows WHERE tmdbId = :showId")
    fun getShowWithDetails(showId: Int): Flow<ShowWithDetails?>

    @Upsert
    suspend fun upsertShow(show: TrackedShowEntity)

    @Upsert
    suspend fun upsertShows(shows: List<TrackedShowEntity>)

    @Query("UPDATE tracked_shows SET isFavorite = :isFavorite, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :showId")
    suspend fun setShowFavorite(showId: Int, isFavorite: Boolean, lastModifiedAt: Instant)

    @Query("UPDATE tracked_shows SET lastModifiedAt = :lastModifiedAt WHERE tmdbId = :showId")
    suspend fun touchLastModified(showId: Int, lastModifiedAt: Instant)

    @Query("UPDATE tracked_shows SET status = :status, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :showId")
    suspend fun setShowStatus(showId: Int, status: WatchStatus, lastModifiedAt: Instant)

    @Delete
    suspend fun deleteShow(show: TrackedShowEntity)
}
