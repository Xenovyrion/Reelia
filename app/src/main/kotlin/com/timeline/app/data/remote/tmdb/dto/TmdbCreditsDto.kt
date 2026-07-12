package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbCreditsDto(
    val id: Int,
    val cast: List<TmdbCastMemberDto> = emptyList(),
    val crew: List<TmdbCrewMemberDto> = emptyList(),
)

@Serializable
data class TmdbCastMemberDto(
    val id: Int,
    val name: String,
    val character: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    val order: Int = Int.MAX_VALUE,
)

@Serializable
data class TmdbCrewMemberDto(
    val id: Int,
    val name: String,
    val job: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
)
