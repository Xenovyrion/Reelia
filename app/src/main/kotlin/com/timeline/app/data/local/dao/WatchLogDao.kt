package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLogDao {
    @Insert
    suspend fun insert(entry: WatchLogEntryEntity)

    @Query("SELECT COUNT(*) FROM watch_log WHERE (:mediaType IS NULL OR mediaType = :mediaType)")
    fun countEntries(mediaType: MediaType? = null): Flow<Int>

    @Query("SELECT COALESCE(SUM(runtimeMinutes), 0) FROM watch_log WHERE (:mediaType IS NULL OR mediaType = :mediaType)")
    fun totalMinutesWatched(mediaType: MediaType? = null): Flow<Int>

    @Query(
        """
        SELECT strftime('%Y-W%W', watchedAt / 1000, 'unixepoch') AS bucket,
               COALESCE(SUM(runtimeMinutes), 0) AS totalMinutes, COUNT(*) AS totalCount
        FROM watch_log
        WHERE (:mediaType IS NULL OR mediaType = :mediaType)
        GROUP BY bucket
        ORDER BY bucket DESC
        LIMIT :limit
        """,
    )
    fun getWeeklyBreakdown(mediaType: MediaType?, limit: Int = 12): Flow<List<TimeBucketStat>>

    @Query(
        """
        SELECT strftime('%Y-%m', watchedAt / 1000, 'unixepoch') AS bucket,
               COALESCE(SUM(runtimeMinutes), 0) AS totalMinutes, COUNT(*) AS totalCount
        FROM watch_log
        WHERE (:mediaType IS NULL OR mediaType = :mediaType)
        GROUP BY bucket
        ORDER BY bucket DESC
        LIMIT :limit
        """,
    )
    fun getMonthlyBreakdown(mediaType: MediaType?, limit: Int = 12): Flow<List<TimeBucketStat>>
}
