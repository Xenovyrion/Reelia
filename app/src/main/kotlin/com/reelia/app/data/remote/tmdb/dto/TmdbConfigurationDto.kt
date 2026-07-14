package com.reelia.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbConfigurationDto(
    val images: TmdbImagesConfigDto,
)

@Serializable
data class TmdbImagesConfigDto(
    @SerialName("secure_base_url") val secureBaseUrl: String,
    @SerialName("poster_sizes") val posterSizes: List<String> = emptyList(),
)
