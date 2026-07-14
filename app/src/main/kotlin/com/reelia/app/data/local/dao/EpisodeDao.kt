package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.reelia.app.data.local.entity.EpisodeEntity
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

    // seasonNumber != 0 excludes TMDB "Specials" from progress/completion — a special left
    // unwatched must never keep a show from reading as 100% watched.
    @Query("SELECT showId, COUNT(*) AS total, SUM(watched) AS watchedCount FROM episodes WHERE seasonNumber != 0 GROUP BY showId")
    fun getEpisodeProgressByShow(): Flow<List<ShowEpisodeProgress>>

    @Query(
        "SELECT showId, COUNT(*) AS total, SUM(watched) AS watchedCount FROM episodes " +
            "WHERE showId = :showId AND seasonNumber != 0 GROUP BY showId",
    )
    suspend fun getEpisodeProgressForShowOnce(showId: Int): ShowEpisodeProgress?

    /** Ordered so the first row per showId (grouped in-memory) is that show's next
     * unwatched episode — libraries here are small, no need for fancier per-group SQL.
     * Specials (season 0) are excluded so they never surface as the "next episode" to watch. */
    @Query("SELECT * FROM episodes WHERE watched = 0 AND seasonNumber != 0 ORDER BY showId, seasonNumber, episodeNumber")
    fun getAllUnwatchedEpisodesOrdered(): Flow<List<EpisodeEntity>>

    /** Backs the library's local text search (title/episode name) — a lightweight projection
     * over every episode of every tracked show, matched in Kotlin rather than per-keystroke SQL. */
    @Query("SELECT showId, name FROM episodes")
    fun getAllEpisodeNames(): Flow<List<EpisodeNameRow>>

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
