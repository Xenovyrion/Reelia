package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSearchResponseDto(
    val results: List<TmdbSearchResultDto> = emptyList(),
)

@Serializable
data class TmdbSearchResultDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String? = null,
    val name: String? = null,
    val title: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val overview: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
)

/** Response shape of TMDB's `/find/{externalId}` endpoint — only the `tv_results` bucket is
 * used (for matching TV Time's TheTVDB show ids to TMDB ids), the other buckets are omitted. */
@Serializable
data class TmdbFindResponseDto(
    @SerialName("tv_results") val tvResults: List<TmdbSearchResultDto> = emptyList(),
)
