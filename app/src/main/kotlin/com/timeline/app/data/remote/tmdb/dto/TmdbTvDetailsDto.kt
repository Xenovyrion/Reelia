package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbTvDetailsDto(
    val id: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerialName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    val genres: List<TmdbGenreDto> = emptyList(),
    val seasons: List<TmdbSeasonSummaryDto> = emptyList(),
    @SerialName("next_episode_to_air") val nextEpisodeToAir: TmdbEpisodeSummaryDto? = null,
    val networks: List<TmdbNetworkDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Float? = null,
)

@Serializable
data class TmdbNetworkDto(
    val id: Int,
    val name: String,
    @SerialName("logo_path") val logoPath: String? = null,
)

@Serializable
data class TmdbSeasonSummaryDto(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String,
    @SerialName("episode_count") val episodeCount: Int = 0,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("air_date") val airDate: String? = null,
)

@Serializable
data class TmdbEpisodeSummaryDto(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String,
    @SerialName("air_date") val airDate: String? = null,
    val runtime: Int? = null,
)

@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String,
)
