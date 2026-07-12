package com.timeline.app.domain.model

data class WatchProviderOption(
    val providerId: Int,
    val providerName: String,
    val logoPath: String?,
)

data class WatchProviders(
    val country: String,
    val link: String?,
    val flatrate: List<WatchProviderOption>,
    val rent: List<WatchProviderOption>,
    val buy: List<WatchProviderOption>,
)
