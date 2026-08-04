package com.reelia.app.data.backup

import kotlinx.serialization.Serializable

/** Bumped whenever a field is added/removed/changed in a way older app versions can't parse.
 * Imports from a newer schema than this app understands are rejected rather than silently
 * dropping fields — see [LibraryBackupRepository.import]. */
const val BACKUP_SCHEMA_VERSION = 1

/**
 * Full local-library snapshot, independent of Firestore sync — exported as a single JSON file
 * so the library can be archived or moved to a fresh account without relying on cloud sync at
 * all. Timestamps are epoch millis (not [java.time.Instant] directly, which kotlinx.serialization
 * doesn't handle out of the box) to mirror how Room's own [com.reelia.app.data.local.Converters]
 * stores them.
 */
@Serializable
data class LibraryBackup(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val exportedAt: Long,
    val appVersion: String,
    val shows: List<BackupShow> = emptyList(),
    val movies: List<BackupMovie> = emptyList(),
    val watchLog: List<BackupWatchLogEntry> = emptyList(),
)

@Serializable
data class BackupShow(
    val tmdbId: Int,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val firstAirDate: String?,
    val status: String,
    val userRating: Float?,
    val addedAt: Long,
    val lastUpdatedAt: Long,
    val numberOfSeasons: Int,
    val nextEpisodeToAirDate: String?,
    val nextEpisodeToAirName: String?,
    val nextEpisodeSeasonNumber: Int?,
    val nextEpisodeNumber: Int?,
    val averageEpisodeRuntimeMinutes: Int?,
    val networkNames: String?,
    val broadcastStatus: String?,
    val lastAirDate: String?,
    val creatorNames: String?,
    val isFavorite: Boolean,
    val lastModifiedAt: Long,
    val contentRating: String?,
    val episodes: List<BackupEpisode> = emptyList(),
)

@Serializable
data class BackupEpisode(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val tmdbEpisodeId: Int,
    val name: String,
    val overview: String?,
    val voteAverage: Float?,
    val airDate: String?,
    val runtimeMinutes: Int?,
    val stillPath: String?,
    val watched: Boolean,
    val watchedAt: Long?,
)

@Serializable
data class BackupMovie(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val status: String,
    val userRating: Float?,
    val addedAt: Long,
    val watched: Boolean,
    val watchedAt: Long?,
    val isFavorite: Boolean,
    val lastModifiedAt: Long,
    val contentRating: String?,
)

@Serializable
data class BackupWatchLogEntry(
    val syncId: String,
    val mediaType: String,
    val tmdbId: Int,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val runtimeMinutes: Int,
    val watchedAt: Long,
)

data class BackupImportReport(
    val showCount: Int,
    val episodeCount: Int,
    val movieCount: Int,
    val newWatchLogEntryCount: Int,
    val exportedAt: Long,
)
