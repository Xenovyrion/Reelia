package com.timeline.app.data.metadata.tmdb

import com.timeline.app.data.metadata.MetadataProvider
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.dto.TmdbCreditsDto
import com.timeline.app.data.remote.tmdb.dto.TmdbTrendingItemDto
import com.timeline.app.data.remote.tmdb.dto.TmdbWatchProviderDto
import com.timeline.app.data.remote.tmdb.dto.TmdbWatchProvidersResponseDto
import com.timeline.app.data.repository.SearchRepository
import com.timeline.app.domain.model.CastMember
import com.timeline.app.domain.model.CrewMember
import com.timeline.app.domain.model.MediaPreview
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.TmdbSearchResult
import com.timeline.app.domain.model.WatchProviderOption
import com.timeline.app.domain.model.WatchProviders
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

    override suspend fun getShowPreview(tmdbId: Int): MediaPreview = coroutineScope {
        val detailsDeferred = async { tmdbApi.getTvDetails(tmdbId) }
        val creditsDeferred = async { tmdbApi.getTvCredits(tmdbId) }
        val watchProvidersDeferred = async { tmdbApi.getTvWatchProviders(tmdbId) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val watchProviders = watchProvidersDeferred.await()

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
        )
    }

    override suspend fun getMoviePreview(tmdbId: Int): MediaPreview = coroutineScope {
        val detailsDeferred = async { tmdbApi.getMovieDetails(tmdbId) }
        val creditsDeferred = async { tmdbApi.getMovieCredits(tmdbId) }
        val watchProvidersDeferred = async { tmdbApi.getMovieWatchProviders(tmdbId) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val watchProviders = watchProvidersDeferred.await()

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
        )
    }
}

private fun TmdbTrendingItemDto.toMediaPreviewOrNull(): MediaPreview? {
    val type = when (mediaType) {
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
    )
}

private fun TmdbCreditsDto.toCastMembers(): List<CastMember> =
    cast.sortedBy { it.order }.take(20).map {
        CastMember(id = it.id, name = it.name, character = it.character, profilePath = it.profilePath)
    }

private fun TmdbCreditsDto.toCrewMembers(): List<CrewMember> =
    crew.filter { it.job == "Director" || it.job == "Creator" }.map {
        CrewMember(id = it.id, name = it.name, job = it.job, profilePath = it.profilePath)
    }

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
