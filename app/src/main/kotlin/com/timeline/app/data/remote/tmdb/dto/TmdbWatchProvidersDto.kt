package com.timeline.app.data.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbWatchProvidersResponseDto(
    val id: Int,
    val results: Map<String, TmdbCountryWatchProvidersDto> = emptyMap(),
)

@Serializable
data class TmdbCountryWatchProvidersDto(
    val link: String? = null,
    val flatrate: List<TmdbWatchProviderDto> = emptyList(),
    val rent: List<TmdbWatchProviderDto> = emptyList(),
    val buy: List<TmdbWatchProviderDto> = emptyList(),
)

@Serializable
data class TmdbWatchProviderDto(
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("logo_path") val logoPath: String? = null,
)
