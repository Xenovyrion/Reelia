package com.reelia.app.data.remote.wikidata

import com.reelia.app.data.remote.wikidata.dto.WikidataSparqlResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Wikidata's free, keyless public SPARQL endpoint — used only for awards/nominations data,
 * which TMDB doesn't provide at all. */
interface WikidataApi {
    @GET("sparql")
    suspend fun query(
        @Query("query") sparql: String,
        @Query("format") format: String = "json",
    ): WikidataSparqlResponseDto

    companion object {
        const val BASE_URL = "https://query.wikidata.org/"
    }
}
