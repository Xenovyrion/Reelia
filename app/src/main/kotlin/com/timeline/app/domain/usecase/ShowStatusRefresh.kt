package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.domain.model.WatchStatus
import java.time.Instant

/** Recomputes and persists a show's WatchStatus from real episode-watch progress — the field
 * otherwise only ever gets set once (PLAN_TO_WATCH) when the show is first added, and never
 * reflects actual watching progress on its own. Shared by [MarkEpisodeWatchedUseCase] and
 * [MarkSeasonWatchedUseCase]. */
internal suspend fun refreshComputedStatus(episodeDao: EpisodeDao, showDao: ShowDao, showId: Int, now: Instant) {
    val progress = episodeDao.getEpisodeProgressForShowOnce(showId) ?: return
    if (progress.total == 0) return
    val newStatus = when {
        progress.watchedCount == progress.total -> WatchStatus.COMPLETED
        progress.watchedCount > 0 -> WatchStatus.WATCHING
        else -> WatchStatus.PLAN_TO_WATCH
    }
    showDao.setShowStatus(showId, newStatus, now)
}
