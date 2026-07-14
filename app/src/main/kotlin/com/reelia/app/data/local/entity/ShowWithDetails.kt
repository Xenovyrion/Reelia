package com.reelia.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ShowWithDetails(
    @Embedded val show: TrackedShowEntity,
    @Relation(parentColumn = "tmdbId", entityColumn = "showId")
    val seasons: List<SeasonEntity>,
    @Relation(parentColumn = "tmdbId", entityColumn = "showId")
    val episodes: List<EpisodeEntity>,
)
