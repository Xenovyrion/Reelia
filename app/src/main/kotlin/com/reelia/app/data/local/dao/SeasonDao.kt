package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.reelia.app.data.local.entity.SeasonEntity

@Dao
interface SeasonDao {
    @Upsert
    suspend fun upsertSeasons(seasons: List<SeasonEntity>)

    /** Finds seasons whose metadata (name, poster, episode count) was persisted from TMDB but
     * whose actual episode rows never landed — the signature left behind when a Firestore-
     * triggered library resync hydrates a show's season list successfully but the follow-up
     * per-season episode fetch fails (e.g. timing out behind too many concurrent TMDB calls).
     * Used to repair those shows without re-fetching seasons/episodes that are already fine. */
    @Query(
        """
        SELECT seasons.showId AS showId, seasons.seasonNumber AS seasonNumber
        FROM seasons
        LEFT JOIN (
            SELECT showId, seasonNumber, COUNT(*) AS episodeRowCount
            FROM episodes
            GROUP BY showId, seasonNumber
        ) episodeCounts
        ON seasons.showId = episodeCounts.showId AND seasons.seasonNumber = episodeCounts.seasonNumber
        WHERE seasons.episodeCount > 0 AND (episodeCounts.episodeRowCount IS NULL OR episodeCounts.episodeRowCount = 0)
        """,
    )
    suspend fun getSeasonsMissingEpisodes(): List<SeasonMissingEpisodes>
}

