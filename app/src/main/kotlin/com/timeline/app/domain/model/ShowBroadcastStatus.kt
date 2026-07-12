package com.timeline.app.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.timeline.app.R

/** The show's own broadcast state on TMDB (e.g. "Ended", "Returning Series") — entirely
 * separate from [WatchStatus], which is the user's personal tracking status. */
enum class ShowBroadcastStatus {
    RETURNING,
    ENDED,
    CANCELED,
    IN_PRODUCTION,
    UNKNOWN,
}

fun parseShowBroadcastStatus(raw: String?): ShowBroadcastStatus = when (raw) {
    "Returning Series" -> ShowBroadcastStatus.RETURNING
    "Ended" -> ShowBroadcastStatus.ENDED
    "Canceled" -> ShowBroadcastStatus.CANCELED
    "In Production", "Planned", "Pilot" -> ShowBroadcastStatus.IN_PRODUCTION
    else -> ShowBroadcastStatus.UNKNOWN
}

@Composable
fun ShowBroadcastStatus.displayLabel(): String = stringResource(
    when (this) {
        ShowBroadcastStatus.RETURNING -> R.string.show_broadcast_status_returning
        ShowBroadcastStatus.ENDED -> R.string.show_broadcast_status_ended
        ShowBroadcastStatus.CANCELED -> R.string.show_broadcast_status_canceled
        ShowBroadcastStatus.IN_PRODUCTION -> R.string.show_broadcast_status_in_production
        ShowBroadcastStatus.UNKNOWN -> R.string.show_broadcast_status_unknown
    },
)
