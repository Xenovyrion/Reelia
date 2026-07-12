package com.timeline.app.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted

/**
 * Single source of truth mapping a [WatchStatus] to one of the app's four functional
 * status colors, so every component (poster dot, progress ring, badge, stat card) stays
 * visually consistent. The favorite flag is a separate, orthogonal concept (its own
 * `StatusFavorite` color) and is never folded into this mapping.
 */
@Composable
fun WatchStatus.statusColor(): Color = when (this) {
    WatchStatus.WATCHING, WatchStatus.COMPLETED -> StatusWatchingCompleted
    WatchStatus.PLAN_TO_WATCH -> StatusWantToWatch
    WatchStatus.ON_HOLD, WatchStatus.DROPPED -> StatusPlanned
}
