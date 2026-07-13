package com.timeline.app.ui.common

import com.timeline.app.data.local.dao.ShowEpisodeProgress
import com.timeline.app.domain.model.WatchStatus

/** The WatchStatus a show/movie should be *displayed* as, derived live from real watch
 * progress rather than trusting the persisted `status` column alone — progress data is proven
 * to update reactively without an app restart (it already backs the poster progress ring), so
 * deriving the badge/grouping from it here guarantees it's never stuck showing stale state. */
fun effectiveShowStatus(rawStatus: WatchStatus, progress: ShowEpisodeProgress?): WatchStatus =
    effectiveShowStatus(rawStatus, totalEpisodes = progress?.total ?: 0, watchedEpisodes = progress?.watchedCount ?: 0)

fun effectiveShowStatus(rawStatus: WatchStatus, totalEpisodes: Int, watchedEpisodes: Int): WatchStatus = when {
    totalEpisodes == 0 -> rawStatus
    watchedEpisodes == totalEpisodes -> WatchStatus.COMPLETED
    watchedEpisodes > 0 -> WatchStatus.WATCHING
    else -> rawStatus
}

fun effectiveMovieStatus(rawStatus: WatchStatus, watched: Boolean): WatchStatus =
    if (watched) WatchStatus.COMPLETED else rawStatus
