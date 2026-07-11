package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject

/**
 * Marks every episode in a season watched, logging one [WatchLogEntryEntity] per episode that
 * was actually unwatched -> watched. Reads the unwatched list BEFORE the bulk update so already-
 * watched episodes are never double-logged.
 */
class MarkSeasonWatchedUseCase @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
) {
    suspend operator fun invoke(showId: Int, seasonNumber: Int) {
        val unwatchedEpisodes = episodeDao.getUnwatchedEpisodesInSeason(showId, seasonNumber)
        if (unwatchedEpisodes.isEmpty()) return

        val watchedAt = Instant.now()
        episodeDao.setSeasonWatched(showId, seasonNumber, watched = true, watchedAt = watchedAt)

        unwatchedEpisodes.forEach { episode ->
            watchLogDao.insert(
                WatchLogEntryEntity(
                    mediaType = MediaType.TV,
                    tmdbId = showId,
                    seasonNumber = seasonNumber,
                    episodeNumber = episode.episodeNumber,
                    runtimeMinutes = episode.runtimeMinutes ?: 0,
                    watchedAt = watchedAt,
                ),
            )
        }
    }
}
