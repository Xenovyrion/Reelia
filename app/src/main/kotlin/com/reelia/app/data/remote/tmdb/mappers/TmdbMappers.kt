package com.reelia.app.data.remote.tmdb.mappers

import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.GenreEntity
import com.reelia.app.data.local.entity.SeasonEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.remote.tmdb.dto.TmdbMovieDetailsDto
import com.reelia.app.data.remote.tmdb.dto.TmdbMovieReleaseDatesResponseDto
import com.reelia.app.data.remote.tmdb.dto.TmdbSeasonDetailsDto
import com.reelia.app.data.remote.tmdb.dto.TmdbTvContentRatingsResponseDto
import com.reelia.app.data.remote.tmdb.dto.TmdbTvDetailsDto
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant

/** Not every title has a French rating on TMDB, so US is the fallback — checked in priority
 * order and the first non-blank certification wins. */
private val CERTIFICATION_REGIONS = listOf("FR", "US")

fun TmdbMovieReleaseDatesResponseDto.toContentRating(): String? =
    CERTIFICATION_REGIONS.firstNotNullOfOrNull { region ->
        results.find { it.countryCode == region }
            ?.releaseDates
            ?.map { it.certification }
            ?.firstOrNull { it.isNotBlank() }
    }

fun TmdbTvContentRatingsResponseDto.toContentRating(): String? =
    CERTIFICATION_REGIONS.firstNotNullOfOrNull { region ->
        results.find { it.countryCode == region }?.rating?.takeIf { it.isNotBlank() }
    }

fun TmdbTvDetailsDto.toEntity(status: WatchStatus, addedAt: Instant, contentRating: String? = null): TrackedShowEntity =
    TrackedShowEntity(
        tmdbId = id,
        name = name,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        firstAirDate = firstAirDate,
        status = status,
        userRating = null,
        addedAt = addedAt,
        lastUpdatedAt = Instant.now(),
        numberOfSeasons = numberOfSeasons,
        nextEpisodeToAirDate = nextEpisodeToAir?.airDate,
        nextEpisodeToAirName = nextEpisodeToAir?.name,
        nextEpisodeSeasonNumber = nextEpisodeToAir?.seasonNumber,
        nextEpisodeNumber = nextEpisodeToAir?.episodeNumber,
        averageEpisodeRuntimeMinutes = episodeRunTime.firstOrNull(),
        networkNames = networks.map { it.name }.takeIf { it.isNotEmpty() }?.joinToString(", "),
        broadcastStatus = this.status,
        lastAirDate = lastAirDate,
        creatorNames = createdBy.map { it.name }.takeIf { it.isNotEmpty() }?.joinToString(", "),
        contentRating = contentRating,
    )

fun TmdbTvDetailsDto.toSeasonEntities(): List<SeasonEntity> =
    seasons
        .map { season ->
            SeasonEntity(
                showId = id,
                seasonNumber = season.seasonNumber,
                name = season.name,
                episodeCount = season.episodeCount,
                posterPath = season.posterPath,
                airDate = season.airDate,
            )
        }

fun TmdbTvDetailsDto.toGenreEntities(): List<GenreEntity> =
    genres.map { GenreEntity(tmdbId = it.id, name = it.name) }

fun TmdbSeasonDetailsDto.toEpisodeEntities(showId: Int, defaultRuntimeMinutes: Int?): List<EpisodeEntity> =
    episodes.map { episode ->
        EpisodeEntity(
            showId = showId,
            seasonNumber = seasonNumber,
            episodeNumber = episode.episodeNumber,
            tmdbEpisodeId = episode.id,
            name = episode.name,
            overview = episode.overview,
            voteAverage = episode.voteAverage,
            airDate = episode.airDate,
            runtimeMinutes = episode.runtime ?: defaultRuntimeMinutes,
            stillPath = episode.stillPath,
        )
    }

fun TmdbMovieDetailsDto.toEntity(status: WatchStatus, addedAt: Instant, contentRating: String? = null): TrackedMovieEntity =
    TrackedMovieEntity(
        tmdbId = id,
        title = title,
        posterPath = posterPath,
        overview = overview,
        releaseDate = releaseDate,
        runtimeMinutes = runtime,
        status = status,
        userRating = null,
        addedAt = addedAt,
        contentRating = contentRating,
    )

fun TmdbMovieDetailsDto.toGenreEntities(): List<GenreEntity> =
    genres.map { GenreEntity(tmdbId = it.id, name = it.name) }
