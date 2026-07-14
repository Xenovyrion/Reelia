package com.timeline.app.data.announcement

import kotlinx.serialization.Serializable

/** Shape of docs/announcement.json — edited directly on GitHub to broadcast a message to every
 * install without an app update. Empty `message` values (the default, checked-in state) mean
 * "nothing to announce". */
@Serializable
data class AnnouncementDto(
    val id: String = "",
    val important: Boolean = false,
    val message: Map<String, String> = emptyMap(),
)

data class Announcement(val id: String, val important: Boolean, val message: String)
