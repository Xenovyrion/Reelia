package com.reelia.app.domain.usecase

import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.WatchLogEntryEntity
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.RuntimeDefaults
import java.time.Instant
import javax.inject.Inject

/**
 * Marks every episode in a season watched or unwatched. When marking watched, logs one
 * [WatchLogEntryEntity] per episode that was actually unwatched -> watched — the unwatched list
 * is read BEFORE the bulk update so already-watched episodes are never double-logged. When
 * marking unwatched, no log entries are removed (mirrors MarkEpisodeWatchedUseCase: the watch
 * log is an append-only history, independent of the current watched flag). Also bumps the
 * parent show's `lastModifiedAt` and pushes it — see MarkEpisodeWatchedUseCase's kdoc for why.
 */
class MarkSeasonWatchedUseCase @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
    private val showDao: ShowDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    suspend operator fun invoke(showId: Int, seasonNumber: Int, watched: Boolean) {
        val now = Instant.now()

        if (watched) {
            val unwatchedEpisodes = episodeDao.getUnwatchedEpisodesInSeason(showId, seasonNumber)
            if (unwatchedEpisodes.isEmpty()) return

            episodeDao.setSeasonWatched(showId, seasonNumber, watched = true, watchedAt = now)

            unwatchedEpisodes.forEach { episode ->
                val entry = WatchLogEntryEntity(
                    mediaType = MediaType.TV,
                    tmdbId = showId,
                    seasonNumber = seasonNumber,
                    episodeNumber = episode.episodeNumber,
                    runtimeMinutes = episode.runtimeMinutes ?: RuntimeDefaults.DEFAULT_EPISODE_RUNTIME_MINUTES,
                    watchedAt = now,
                )
                watchLogDao.insert(entry)
                firestoreSyncRepository.pushWatchLogEntry(entry)
            }
        } else {
            episodeDao.setSeasonWatched(showId, seasonNumber, watched = false, watchedAt = null)
        }

        refreshComputedStatus(episodeDao, showDao, showId, now)
        showDao.touchLastModified(showId, now)
        syncOutboxDao.markPending(SyncOutboxEntity(showId, MediaType.TV, now))
        firestoreSyncRepository.pushPendingChanges()
    }
}
