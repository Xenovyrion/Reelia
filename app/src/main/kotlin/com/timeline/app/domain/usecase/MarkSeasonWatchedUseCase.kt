package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.SyncOutboxDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.SyncOutboxEntity
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.data.sync.FirestoreSyncRepository
import com.timeline.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject

/**
 * Marks every episode in a season watched, logging one [WatchLogEntryEntity] per episode that
 * was actually unwatched -> watched. Reads the unwatched list BEFORE the bulk update so already-
 * watched episodes are never double-logged. Also bumps the parent show's `lastModifiedAt` and
 * pushes it — see MarkEpisodeWatchedUseCase's kdoc for why.
 */
class MarkSeasonWatchedUseCase @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
    private val showDao: ShowDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    suspend operator fun invoke(showId: Int, seasonNumber: Int) {
        val unwatchedEpisodes = episodeDao.getUnwatchedEpisodesInSeason(showId, seasonNumber)
        if (unwatchedEpisodes.isEmpty()) return

        val watchedAt = Instant.now()
        episodeDao.setSeasonWatched(showId, seasonNumber, watched = true, watchedAt = watchedAt)

        unwatchedEpisodes.forEach { episode ->
            val entry = WatchLogEntryEntity(
                mediaType = MediaType.TV,
                tmdbId = showId,
                seasonNumber = seasonNumber,
                episodeNumber = episode.episodeNumber,
                runtimeMinutes = episode.runtimeMinutes ?: 0,
                watchedAt = watchedAt,
            )
            watchLogDao.insert(entry)
            firestoreSyncRepository.pushWatchLogEntry(entry)
        }

        showDao.touchLastModified(showId, watchedAt)
        syncOutboxDao.markPending(SyncOutboxEntity(showId, MediaType.TV, watchedAt))
        firestoreSyncRepository.pushPendingChanges()
    }
}
