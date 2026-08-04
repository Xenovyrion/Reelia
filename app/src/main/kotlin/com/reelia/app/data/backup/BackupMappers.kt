package com.reelia.app.data.backup

import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.local.entity.WatchLogEntryEntity
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant

fun TrackedShowEntity.toBackup(episodes: List<EpisodeEntity>): BackupShow = BackupShow(
    tmdbId = tmdbId,
    name = name,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    firstAirDate = firstAirDate,
    status = status.name,
    userRating = userRating,
    addedAt = addedAt.toEpochMilli(),
    lastUpdatedAt = lastUpdatedAt.toEpochMilli(),
    numberOfSeasons = numberOfSeasons,
    nextEpisodeToAirDate = nextEpisodeToAirDate,
    nextEpisodeToAirName = nextEpisodeToAirName,
    nextEpisodeSeasonNumber = nextEpisodeSeasonNumber,
    nextEpisodeNumber = nextEpisodeNumber,
    averageEpisodeRuntimeMinutes = averageEpisodeRuntimeMinutes,
    networkNames = networkNames,
    broadcastStatus = broadcastStatus,
    lastAirDate = lastAirDate,
    creatorNames = creatorNames,
    isFavorite = isFavorite,
    lastModifiedAt = lastModifiedAt.toEpochMilli(),
    contentRating = contentRating,
    episodes = episodes.map { it.toBackup() },
)

fun EpisodeEntity.toBackup(): BackupEpisode = BackupEpisode(
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    tmdbEpisodeId = tmdbEpisodeId,
    name = name,
    overview = overview,
    voteAverage = voteAverage,
    airDate = airDate,
    runtimeMinutes = runtimeMinutes,
    stillPath = stillPath,
    watched = watched,
    watchedAt = watchedAt?.toEpochMilli(),
)

fun TrackedMovieEntity.toBackup(): BackupMovie = BackupMovie(
    tmdbId = tmdbId,
    title = title,
    posterPath = posterPath,
    overview = overview,
    releaseDate = releaseDate,
    runtimeMinutes = runtimeMinutes,
    status = status.name,
    userRating = userRating,
    addedAt = addedAt.toEpochMilli(),
    watched = watched,
    watchedAt = watchedAt?.toEpochMilli(),
    isFavorite = isFavorite,
    lastModifiedAt = lastModifiedAt.toEpochMilli(),
    contentRating = contentRating,
)

fun WatchLogEntryEntity.toBackup(): BackupWatchLogEntry = BackupWatchLogEntry(
    syncId = syncId,
    mediaType = mediaType.name,
    tmdbId = tmdbId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    runtimeMinutes = runtimeMinutes,
    watchedAt = watchedAt.toEpochMilli(),
)

fun BackupShow.toEntity(): TrackedShowEntity = TrackedShowEntity(
    tmdbId = tmdbId,
    name = name,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    firstAirDate = firstAirDate,
    status = WatchStatus.valueOf(status),
    userRating = userRating,
    addedAt = Instant.ofEpochMilli(addedAt),
    lastUpdatedAt = Instant.ofEpochMilli(lastUpdatedAt),
    numberOfSeasons = numberOfSeasons,
    nextEpisodeToAirDate = nextEpisodeToAirDate,
    nextEpisodeToAirName = nextEpisodeToAirName,
    nextEpisodeSeasonNumber = nextEpisodeSeasonNumber,
    nextEpisodeNumber = nextEpisodeNumber,
    averageEpisodeRuntimeMinutes = averageEpisodeRuntimeMinutes,
    networkNames = networkNames,
    broadcastStatus = broadcastStatus,
    lastAirDate = lastAirDate,
    creatorNames = creatorNames,
    isFavorite = isFavorite,
    lastModifiedAt = Instant.ofEpochMilli(lastModifiedAt),
    contentRating = contentRating,
)

fun BackupEpisode.toEntity(showId: Int): EpisodeEntity = EpisodeEntity(
    showId = showId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    tmdbEpisodeId = tmdbEpisodeId,
    name = name,
    overview = overview,
    voteAverage = voteAverage,
    airDate = airDate,
    runtimeMinutes = runtimeMinutes,
    stillPath = stillPath,
    watched = watched,
    watchedAt = watchedAt?.let(Instant::ofEpochMilli),
)

fun BackupMovie.toEntity(): TrackedMovieEntity = TrackedMovieEntity(
    tmdbId = tmdbId,
    title = title,
    posterPath = posterPath,
    overview = overview,
    releaseDate = releaseDate,
    runtimeMinutes = runtimeMinutes,
    status = WatchStatus.valueOf(status),
    userRating = userRating,
    addedAt = Instant.ofEpochMilli(addedAt),
    watched = watched,
    watchedAt = watchedAt?.let(Instant::ofEpochMilli),
    isFavorite = isFavorite,
    lastModifiedAt = Instant.ofEpochMilli(lastModifiedAt),
    contentRating = contentRating,
)

fun BackupWatchLogEntry.toEntity(): WatchLogEntryEntity = WatchLogEntryEntity(
    syncId = syncId,
    mediaType = MediaType.valueOf(mediaType),
    tmdbId = tmdbId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    runtimeMinutes = runtimeMinutes,
    watchedAt = Instant.ofEpochMilli(watchedAt),
)
