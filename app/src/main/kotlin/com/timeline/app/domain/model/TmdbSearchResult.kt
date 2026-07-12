package com.timeline.app.domain.model

data class TmdbSearchResult(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val date: String?,
)
