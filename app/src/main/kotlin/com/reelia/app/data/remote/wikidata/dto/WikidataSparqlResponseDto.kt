package com.reelia.app.data.remote.wikidata.dto

import kotlinx.serialization.Serializable

@Serializable
data class WikidataSparqlResponseDto(
    val results: WikidataResultsDto = WikidataResultsDto(),
)

@Serializable
data class WikidataResultsDto(
    val bindings: List<Map<String, WikidataValueDto>> = emptyList(),
)

@Serializable
data class WikidataValueDto(
    val value: String,
)
