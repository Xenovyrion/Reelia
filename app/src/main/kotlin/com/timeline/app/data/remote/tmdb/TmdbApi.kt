package com.timeline.app.data.remote.tmdb

import com.timeline.app.data.remote.tmdb.dto.TmdbConfigurationDto
import com.timeline.app.data.remote.tmdb.dto.TmdbCreditsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbFindResponseDto
import com.timeline.app.data.remote.tmdb.dto.TmdbMovieDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbPagedResponseDto
import com.timeline.app.data.remote.tmdb.dto.TmdbPersonCombinedCreditsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbPersonDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbSearchResponseDto
import com.timeline.app.data.remote.tmdb.dto.TmdbSeasonDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbTrendingItemDto
import com.timeline.app.data.remote.tmdb.dto.TmdbTvDetailsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbVideosDto
import com.timeline.app.data.remote.tmdb.dto.TmdbWatchProvidersResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB `language`/`region` query params are injected for every call by [TmdbLanguageInterceptor],
 * so methods here don't declare them individually.
 */
interface TmdbApi {
    @GET("configuration")
    suspend fun getConfiguration(): TmdbConfigurationDto

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbSearchResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbSearchResponseDto

    /** Resolves an external id (e.g. a TheTVDB show id, as used by TV Time's export) to its
     * TMDB match. Used by the TV Time import feature for exact show matching. */
    @GET("find/{externalId}")
    suspend fun findByExternalId(
        @Path("externalId") externalId: Int,
        @Query("external_source") externalSource: String = "tvdb_id",
    ): TmdbFindResponseDto

    @GET("trending/{mediaType}/{timeWindow}")
    suspend fun getTrending(
        @Path("mediaType") mediaType: String = "all",
        @Path("timeWindow") timeWindow: String = "week",
    ): TmdbPagedResponseDto<TmdbTrendingItemDto>

    @GET("tv/{id}")
    suspend fun getTvDetails(@Path("id") id: Int): TmdbTvDetailsDto

    @GET("tv/{id}/season/{seasonNumber}")
    suspend fun getSeasonDetails(
        @Path("id") id: Int,
        @Path("seasonNumber") seasonNumber: Int,
    ): TmdbSeasonDetailsDto

    @GET("tv/{id}/watch/providers")
    suspend fun getTvWatchProviders(@Path("id") id: Int): TmdbWatchProvidersResponseDto

    @GET("tv/{id}/credits")
    suspend fun getTvCredits(@Path("id") id: Int): TmdbCreditsDto

    @GET("tv/{id}/videos")
    suspend fun getTvVideos(@Path("id") id: Int): TmdbVideosDto

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): TmdbMovieDetailsDto

    @GET("movie/{id}/watch/providers")
    suspend fun getMovieWatchProviders(@Path("id") id: Int): TmdbWatchProvidersResponseDto

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(@Path("id") id: Int): TmdbCreditsDto

    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(@Path("id") id: Int): TmdbVideosDto

    /** [language] lets callers force a specific locale (e.g. an English fallback when the
     * user's own locale has no translated biography), overriding the default injected by
     * [TmdbLanguageInterceptor] — leave null for normal locale-aware calls. */
    @GET("person/{id}")
    suspend fun getPersonDetails(
        @Path("id") id: Int,
        @Query("language") language: String? = null,
    ): TmdbPersonDetailsDto

    @GET("person/{id}/combined_credits")
    suspend fun getPersonCombinedCredits(@Path("id") id: Int): TmdbPersonCombinedCreditsDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }
}
