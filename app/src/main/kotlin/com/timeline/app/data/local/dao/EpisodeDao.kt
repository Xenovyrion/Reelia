package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.timeline.app.data.local.entity.EpisodeEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Upsert
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE showId = :showId ORDER BY seasonNumber, episodeNumber")
    fun getEpisodesForShow(showId: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE showId = :showId ORDER BY seasonNumber, episodeNumber")
    suspend fun getEpisodesForShowOnce(showId: Int): List<EpisodeEntity>

    @Query(
        """
        UPDATE episodes SET watched = :watched, watchedAt = :watchedAt
        WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber
        """,
    )
    suspend fun setEpisodeWatched(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        watched: Boolean,
        watchedAt: Instant?,
    )

    @Query("SELECT * FROM episodes WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun getEpisode(showId: Int, seasonNumber: Int, episodeNumber: Int): EpisodeEntity?

    @Query("SELECT showId, COUNT(*) AS total, SUM(watched) AS watchedCount FROM episodes GROUP BY showId")
    fun getEpisodeProgressByShow(): Flow<List<ShowEpisodeProgress>>

    @Query("SELECT showId, COUNT(*) AS total, SUM(watched) AS watchedCount FROM episodes WHERE showId = :showId GROUP BY showId")
    suspend fun getEpisodeProgressForShowOnce(showId: Int): ShowEpisodeProgress?

    /** Ordered so the first row per showId (grouped in-memory) is that show's next
     * unwatched episode — libraries here are small, no need for fancier per-group SQL. */
    @Query("SELECT * FROM episodes WHERE watched = 0 ORDER BY showId, seasonNumber, episodeNumber")
    fun getAllUnwatchedEpisodesOrdered(): Flow<List<EpisodeEntity>>

    @Query(
        "SELECT * FROM episodes WHERE showId = :showId AND seasonNumber = :seasonNumber AND watched = 0",
    )
    suspend fun getUnwatchedEpisodesInSeason(showId: Int, seasonNumber: Int): List<EpisodeEntity>

    @Query(
        """
        UPDATE episodes SET watched = :watched, watchedAt = :watchedAt
        WHERE showId = :showId AND seasonNumber = :seasonNumber
        """,
    )
    suspend fun setSeasonWatched(showId: Int, seasonNumber: Int, watched: Boolean, watchedAt: Instant?)
}
