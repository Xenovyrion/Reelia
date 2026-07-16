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
 * Marks an episode watched/unwatched. Writes both the episode's `watched` flag (for the
 * progress UI) and an append-only [WatchLogEntryEntity] (for stats), since the two are
 * intentionally decoupled — see WatchLogEntryEntity's kdoc. Also bumps the parent show's
 * `lastModifiedAt` and pushes it, since per-episode watched-state syncs as part of the
 * show's own Firestore document (see FirestoreSyncRepository).
 *
 * When [fillGaps] is true and [watched] is true, also marks every earlier unwatched episode in
 * the same season (episodeNumber <= this one) watched, so checking episode 10 after skipping
 * 1-9 catches those up too. [fillGaps] is ignored when unmarking — unchecking always affects
 * only the single episode requested.
 */
class MarkEpisodeWatchedUseCase @Inject constructor(
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
    private val showDao: ShowDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    suspend operator fun invoke(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        watched: Boolean,
        fillGaps: Boolean = false,
    ) {
        val now = Instant.now()

        val episodeNumbersToMark = if (watched && fillGaps) {
            episodeDao.getUnwatchedEpisodesInSeason(showId, seasonNumber)
                .map { it.episodeNumber }
                .filter { it <= episodeNumber }
                .plus(episodeNumber)
                .distinct()
        } else {
            listOf(episodeNumber)
        }

        episodeNumbersToMark.forEach { number ->
            episodeDao.setEpisodeWatched(showId, seasonNumber, number, watched, if (watched) now else null)
        }

        if (watched) {
            episodeNumbersToMark.forEach { number ->
                val episode = episodeDao.getEpisode(showId, seasonNumber, number) ?: return@forEach
                val entry = WatchLogEntryEntity(
                    mediaType = MediaType.TV,
                    tmdbId = showId,
                    seasonNumber = seasonNumber,
                    episodeNumber = number,
                    runtimeMinutes = episode.runtimeMinutes ?: RuntimeDefaults.DEFAULT_EPISODE_RUNTIME_MINUTES,
                    watchedAt = now,
                )
                watchLogDao.insert(entry)
                firestoreSyncRepository.pushWatchLogEntry(entry)
            }
        }

        refreshComputedStatus(episodeDao, showDao, showId, now)
        showDao.touchLastModified(showId, now)
        syncOutboxDao.markPending(SyncOutboxEntity(showId, MediaType.TV, now))
        firestoreSyncRepository.pushPendingChanges()
    }
}
