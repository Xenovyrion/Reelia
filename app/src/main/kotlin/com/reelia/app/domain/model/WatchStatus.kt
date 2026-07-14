package com.reelia.app.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.reelia.app.R

enum class WatchStatus {
    WATCHING,
    PLAN_TO_WATCH,
    COMPLETED,
    DROPPED,
    ON_HOLD,
}

@Composable
fun WatchStatus.displayLabel(): String = stringResource(
    when (this) {
        WatchStatus.WATCHING -> R.string.watch_status_watching
        WatchStatus.PLAN_TO_WATCH -> R.string.watch_status_plan_to_watch
        WatchStatus.COMPLETED -> R.string.watch_status_completed
        WatchStatus.DROPPED -> R.string.watch_status_dropped
        WatchStatus.ON_HOLD -> R.string.watch_status_on_hold
    },
)
