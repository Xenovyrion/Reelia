package com.reelia.app.ui.statsdetail

import com.reelia.app.domain.model.MediaType

enum class StatsDetailFilterType {
    GENRE,
    NETWORK,
    BROADCAST_STATUS,
}

data class LibraryItemSummary(val id: Int, val mediaType: MediaType, val title: String, val posterUrl: String?)
