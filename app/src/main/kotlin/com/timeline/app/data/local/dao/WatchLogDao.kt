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

    @Query("SELECT COUNT(*) FROM watch_log WHERE syncId = :syncId")
    suspend fun countBySyncId(syncId: String): Int

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

    @Query(
        """
        SELECT genreName, COALESCE(SUM(runtimeMinutes), 0) AS totalMinutes FROM (
            SELECT wl.runtimeMinutes AS runtimeMinutes, g.name AS genreName
            FROM watch_log wl
            JOIN show_genre_cross_ref sg ON sg.showId = wl.tmdbId
            JOIN genres g ON g.tmdbId = sg.genreId
            WHERE wl.mediaType = 'TV' AND (:mediaType IS NULL OR wl.mediaType = :mediaType)
            UNION ALL
            SELECT wl.runtimeMinutes AS runtimeMinutes, g.name AS genreName
            FROM watch_log wl
            JOIN movie_genre_cross_ref mg ON mg.movieId = wl.tmdbId
            JOIN genres g ON g.tmdbId = mg.genreId
            WHERE wl.mediaType = 'MOVIE' AND (:mediaType IS NULL OR wl.mediaType = :mediaType)
        )
        GROUP BY genreName
        ORDER BY totalMinutes DESC
        LIMIT :limit
        """,
    )
    fun getGenreBreakdown(mediaType: MediaType? = null, limit: Int = 5): Flow<List<GenreStat>>
}
