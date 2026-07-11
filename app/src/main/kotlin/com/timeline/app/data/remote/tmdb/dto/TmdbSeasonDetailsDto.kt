package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSeasonDetailsDto(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    val episodes: List<TmdbEpisodeDto> = emptyList(),
)

@Serializable
data class TmdbEpisodeDto(
    val id: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String,
    val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Float? = null,
    @SerialName("air_date") val airDate: String? = null,
    val runtime: Int? = null,
)
