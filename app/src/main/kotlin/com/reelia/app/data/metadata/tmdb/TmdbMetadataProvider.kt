package com.reelia.app.data.metadata.tmdb

import com.reelia.app.data.metadata.MetadataProvider
import com.reelia.app.data.remote.tmdb.TmdbApi
import com.reelia.app.data.remote.tmdb.dto.TmdbCreditsDto
import com.reelia.app.data.remote.tmdb.dto.TmdbTrendingItemDto
import com.reelia.app.data.remote.tmdb.dto.TmdbVideosDto
import com.reelia.app.data.remote.tmdb.dto.TmdbWatchProviderDto
import com.reelia.app.data.remote.tmdb.dto.TmdbWatchProvidersResponseDto
import com.reelia.app.data.repository.SearchRepository
import com.reelia.app.domain.model.CastMember
import com.reelia.app.domain.model.CrewMember
import com.reelia.app.domain.model.Genre
import com.reelia.app.domain.model.MediaPreview
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.TmdbSearchResult
import com.reelia.app.domain.model.WatchProviderOption
import com.reelia.app.domain.model.WatchProviders
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val WATCH_PROVIDERS_REGION = "FR"

@Singleton
class TmdbMetadataProvider @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val searchRepository: SearchRepository,
) : MetadataProvider {

    override val id: String = "tmdb"
    override val displayName: String = "TMDB"
    override val isAvailable: Boolean = true

    override suspend fun search(query: String): List<TmdbSearchResult> = searchRepository.searchMulti(query)

    override suspend fun getTrendingFeed(): List<MediaPreview> =
        tmdbApi.getTrending().results.mapNotNull { it.toMediaPreviewOrNull() }

    override suspend fun getRecentMoviesFeed(): List<MediaPreview> =
        tmdbApi.discoverMovies(releaseDateLte = LocalDate.now().toString())
            .results
            .mapNotNull { it.toMediaPreviewOrNull(MediaType.MOVIE) }

    override suspend fun getRecentShowsFeed(): List<MediaPreview> =
        tmdbApi.discoverTv(firstAirDateLte = LocalDate.now().toString())
            .results
            .mapNotNull { it.toMediaPreviewOrNull(MediaType.TV) }

    override suspend fun getGenres(mediaType: MediaType?): List<Genre> = when (mediaType) {
        MediaType.TV -> tmdbApi.getTvGenres().genres.map { Genre(it.id, it.name) }
        MediaType.MOVIE -> tmdbApi.getMovieGenres().genres.map { Genre(it.id, it.name) }
        null -> coroutineScope {
            val movieGenresDeferred = async { tmdbApi.getMovieGenres().genres }
            val tvGenresDeferred = async { tmdbApi.getTvGenres().genres }
            (movieGenresDeferred.await() + tvGenresDeferred.await())
                .distinctBy { it.id }
                .map { Genre(it.id, it.name) }
        }
    }

    override suspend fun getRecommendationsFeed(mediaType: MediaType, tmdbId: Int): List<MediaPreview> {
        val results = when (mediaType) {
            MediaType.MOVIE -> tmdbApi.getMovieRecommendations(tmdbId).results
            MediaType.TV -> tmdbApi.getTvRecommendations(tmdbId).results
        }
        return results.mapNotNull { it.toMediaPreviewOrNull(mediaType) }
    }

    override suspend fun getShowPreview(tmdbId: Int): MediaPreview = coroutineScope {
        val detailsDeferred = async { tmdbApi.getTvDetails(tmdbId) }
        val creditsDeferred = async { tmdbApi.getTvCredits(tmdbId) }
        val watchProvidersDeferred = async { tmdbApi.getTvWatchProviders(tmdbId) }
        val videosDeferred = async { tmdbApi.getTvVideos(tmdbId) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val watchProviders = watchProvidersDeferred.await()
        val videos = videosDeferred.await()

        MediaPreview(
            tmdbId = details.id,
            mediaType = MediaType.TV,
            title = details.name,
            posterPath = details.posterPath,
            backdropPath = details.backdropPath,
            overview = details.overview,
            releaseDate = details.firstAirDate,
            numberOfSeasons = details.numberOfSeasons,
            voteAverage = details.voteAverage,
            genreNames = details.genres.map { it.name },
            networkNames = details.networks.map { it.name },
            cast = credits.toCastMembers(),
            crew = credits.toCrewMembers(),
            watchProviders = watchProviders.toWatchProviders(),
            trailerYoutubeKey = videos.trailerYoutubeKey(),
        )
    }

    override suspend fun getMoviePreview(tmdbId: Int): MediaPreview = coroutineScope {
        val detailsDeferred = async { tmdbApi.getMovieDetails(tmdbId) }
        val creditsDeferred = async { tmdbApi.getMovieCredits(tmdbId) }
        val watchProvidersDeferred = async { tmdbApi.getMovieWatchProviders(tmdbId) }
        val videosDeferred = async { tmdbApi.getMovieVideos(tmdbId) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val watchProviders = watchProvidersDeferred.await()
        val videos = videosDeferred.await()

        MediaPreview(
            tmdbId = details.id,
            mediaType = MediaType.MOVIE,
            title = details.title,
            posterPath = details.posterPath,
            backdropPath = null,
            overview = details.overview,
            releaseDate = details.releaseDate,
            runtimeMinutes = details.runtime,
            voteAverage = details.voteAverage,
            genreNames = details.genres.map { it.name },
            cast = credits.toCastMembers(),
            crew = credits.toCrewMembers(),
            watchProviders = watchProviders.toWatchProviders(),
            trailerYoutubeKey = videos.trailerYoutubeKey(),
        )
    }
}

private fun TmdbTrendingItemDto.toMediaPreviewOrNull(forcedType: MediaType? = null): MediaPreview? {
    val type = forcedType ?: when (mediaType) {
        "tv" -> MediaType.TV
        "movie" -> MediaType.MOVIE
        else -> return null
    }
    val resolvedTitle = (if (type == MediaType.TV) name else title) ?: return null
    return MediaPreview(
        tmdbId = id,
        mediaType = type,
        title = resolvedTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = if (type == MediaType.TV) firstAirDate else releaseDate,
        voteAverage = voteAverage,
        genreIds = genreIds,
    )
}

private fun TmdbCreditsDto.toCastMembers(): List<CastMember> =
    cast.sortedBy { it.order }.take(20).map {
        CastMember(id = it.id, name = it.name, character = it.character, profilePath = it.profilePath)
    }

private val RELEVANT_CREW_JOBS = setOf("Director", "Creator", "Original Music Composer", "Writer")

private fun TmdbCreditsDto.toCrewMembers(): List<CrewMember> =
    crew.filter { it.job in RELEVANT_CREW_JOBS }.map {
        CrewMember(id = it.id, name = it.name, job = it.job, profilePath = it.profilePath)
    }

private fun TmdbVideosDto.trailerYoutubeKey(): String? =
    results
        .filter { it.site == "YouTube" && it.type == "Trailer" }
        .sortedByDescending { it.official }
        .firstOrNull()
        ?.key

private fun TmdbWatchProvidersResponseDto.toWatchProviders(): WatchProviders? {
    val country = results[WATCH_PROVIDERS_REGION] ?: return null
    fun List<TmdbWatchProviderDto>.toOptions() =
        map { WatchProviderOption(providerId = it.providerId, providerName = it.providerName, logoPath = it.logoPath) }
    return WatchProviders(
        country = WATCH_PROVIDERS_REGION,
        link = country.link,
        flatrate = country.flatrate.toOptions(),
        rent = country.rent.toOptions(),
        buy = country.buy.toOptions(),
    )
}
