package com.reelia.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieReleaseDatesResponseDto(
    val results: List<TmdbMovieReleaseDatesCountryDto> = emptyList(),
)

@Serializable
data class TmdbMovieReleaseDatesCountryDto(
    @SerialName("iso_3166_1") val countryCode: String,
    @SerialName("release_dates") val releaseDates: List<TmdbReleaseDateDto> = emptyList(),
)

@Serializable
data class TmdbReleaseDateDto(
    val certification: String = "",
)

@Serializable
data class TmdbTvContentRatingsResponseDto(
    val results: List<TmdbTvContentRatingDto> = emptyList(),
)

@Serializable
data class TmdbTvContentRatingDto(
    @SerialName("iso_3166_1") val countryCode: String,
    val rating: String = "",
)
