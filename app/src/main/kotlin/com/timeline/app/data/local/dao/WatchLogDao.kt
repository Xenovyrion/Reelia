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

    /** Raw (watchedAt, runtimeMinutes) rows — bucketed into weeks/months in Kotlin (see
     * StatsRepository) so periods with zero entries can be backfilled with a zero value instead
     * of being silently absent from the chart. */
    @Query("SELECT watchedAt, runtimeMinutes FROM watch_log WHERE (:mediaType IS NULL OR mediaType = :mediaType)")
    fun getAllEntriesForBreakdown(mediaType: MediaType?): Flow<List<WatchLogTimeEntry>>

    @Query(
        """
        SELECT genreId, genreName, COALESCE(SUM(runtimeMinutes), 0) AS totalMinutes FROM (
            SELECT wl.runtimeMinutes AS runtimeMinutes, g.tmdbId AS genreId, g.name AS genreName
            FROM watch_log wl
            JOIN show_genre_cross_ref sg ON sg.showId = wl.tmdbId
            JOIN genres g ON g.tmdbId = sg.genreId
            WHERE wl.mediaType = 'TV' AND (:mediaType IS NULL OR wl.mediaType = :mediaType)
            UNION ALL
            SELECT wl.runtimeMinutes AS runtimeMinutes, g.tmdbId AS genreId, g.name AS genreName
            FROM watch_log wl
            JOIN movie_genre_cross_ref mg ON mg.movieId = wl.tmdbId
            JOIN genres g ON g.tmdbId = mg.genreId
            WHERE wl.mediaType = 'MOVIE' AND (:mediaType IS NULL OR wl.mediaType = :mediaType)
        )
        GROUP BY genreId, genreName
        ORDER BY totalMinutes DESC
        LIMIT :limit
        """,
    )
    fun getGenreBreakdown(mediaType: MediaType? = null, limit: Int = 5): Flow<List<GenreStat>>
}
