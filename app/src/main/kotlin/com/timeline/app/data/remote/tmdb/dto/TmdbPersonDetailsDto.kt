package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbPersonDetailsDto(
    val id: Int,
    val name: String,
    val biography: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
)
