package com.reelia.app.ui.settings

import com.reelia.app.R

/** Maps a reminder offset (days before release) to the @StringRes label shown in the release
 * reminders checklist — see NotificationPreferenceStore.AVAILABLE_OFFSETS. */
val NOTIFICATION_OFFSET_LABEL_RES: Map<Int, Int> = mapOf(
    30 to R.string.notification_offset_30,
    14 to R.string.notification_offset_14,
    8 to R.string.notification_offset_8,
    1 to R.string.notification_offset_1,
    0 to R.string.notification_offset_0,
)
