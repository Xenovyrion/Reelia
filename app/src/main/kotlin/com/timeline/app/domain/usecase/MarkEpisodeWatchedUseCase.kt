package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject

/**
 * Marks an episode watched/unwatched. Writes both the episode's `watched` flag (for the
 * progress UI) and an append-only [WatchLogEntryEntity] (for stats), since the two are
 * intentionally decoupled — see WatchLogEntryEntity's kdoc.
 */
class MarkEpisodeWatchedUseCase @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
) {
    suspend operator fun invoke(showId: Int, seasonNumber: Int, episodeNumber: Int, watched: Boolean) {
        val watchedAt = if (watched) Instant.now() else null
        episodeDao.setEpisodeWatched(showId, seasonNumber, episodeNumber, watched, watchedAt)

        if (watched) {
            val episode = episodeDao.getEpisode(showId, seasonNumber, episodeNumber) ?: return
            watchLogDao.insert(
                WatchLogEntryEntity(
                    mediaType = MediaType.TV,
                    tmdbId = showId,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    runtimeMinutes = episode.runtimeMinutes ?: 0,
                    watchedAt = watchedAt ?: Instant.now(),
                ),
            )
        }
    }
}
