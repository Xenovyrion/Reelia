package com.timeline.app.data.remote.tmdb

import com.timeline.app.data.remote.tmdb.dto.TmdbConfigurationDto
import com.timeline.app.data.remote.tmdb.dto.TmdbMovieDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbSearchResponseDto
import com.timeline.app.data.remote.tmdb.dto.TmdbSeasonDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbTvDetailsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("configuration")
    suspend fun getConfiguration(): TmdbConfigurationDto

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR",
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbSearchResponseDto

    @GET("tv/{id}")
    suspend fun getTvDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "fr-FR",
    ): TmdbTvDetailsDto

    @GET("tv/{id}/season/{seasonNumber}")
    suspend fun getSeasonDetails(
        @Path("id") id: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Query("language") language: String = "fr-FR",
    ): TmdbSeasonDetailsDto

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "fr-FR",
    ): TmdbMovieDetailsDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }
}
