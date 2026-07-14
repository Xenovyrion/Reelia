package com.reelia.app.data.remote.tmdb.dto

import kotlinx.serialization.Serializable

@Serializable
data class TmdbVideosDto(
    val id: Int = 0,
    val results: List<TmdbVideoDto> = emptyList(),
)

@Serializable
data class TmdbVideoDto(
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean = false,
)
