package com.reelia.app.data.backup

import com.reelia.app.BuildConfig
import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Thrown when the file picked for import isn't a Reelia backup, or is malformed. */
class InvalidBackupException(message: String) : Exception(message)

/** Thrown when a backup was exported by a newer app version whose schema this build doesn't
 * understand — importing it anyway could silently drop or misinterpret fields. */
class UnsupportedBackupVersionException(val foundVersion: Int) :
    Exception("Backup schema version $foundVersion is newer than this app supports ($BACKUP_SCHEMA_VERSION)")

/**
 * Full local-library export/import, independent of Firestore — a standalone JSON snapshot the
 * user can archive outside the app, or use to seed a fresh account without depending on cloud
 * sync at all.
 *
 * Import restores entities directly from the file's own fields rather than re-fetching from
 * TMDB (unlike [com.reelia.app.data.tvtimeimport.TvTimeImportRepository], which has to look
 * titles up since TV Time exports don't carry TMDB ids) — this backup already has everything
 * needed, so a restore is instant and works without a TMDB API key or network at all.
 */
@Singleton
class LibraryBackupRepository @Inject constructor(
    private val showDao: ShowDao,
    private val movieDao: MovieDao,
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    // A dedicated instance rather than the app's shared network Json bean (see NetworkModule) —
    // pretty-printing here makes an exported file readable if the user opens it directly, which
    // isn't a concern for API responses and shouldn't be forced onto every other JSON use in the app.
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun export(): String = withContext(Dispatchers.IO) {
        val shows = showDao.getAllShows().first()
        val movies = movieDao.getAllMovies().first()
        val episodesByShow = episodeDao.getAllEpisodesOnce().groupBy { it.showId }
        val watchLog = watchLogDao.getAllEntriesOnce()

        val backup = LibraryBackup(
            exportedAt = Instant.now().toEpochMilli(),
            appVersion = BuildConfig.VERSION_NAME,
            shows = shows.map { it.toBackup(episodesByShow[it.tmdbId].orEmpty()) },
            movies = movies.map { it.toBackup() },
            watchLog = watchLog.map { it.toBackup() },
        )
        json.encodeToString(backup)
    }

    /** Parses and validates a candidate backup file without writing anything yet, so the UI can
     * show a confirmation summary before committing to the import. */
    suspend fun parse(content: String): LibraryBackup = withContext(Dispatchers.IO) {
        val backup = runCatching { json.decodeFromString<LibraryBackup>(content) }
            .getOrElse { throw InvalidBackupException("Not a valid Reelia backup file") }
        if (backup.schemaVersion > BACKUP_SCHEMA_VERSION) {
            throw UnsupportedBackupVersionException(backup.schemaVersion)
        }
        backup
    }

    /**
     * Upserts every show/movie/episode from [backup] into the local library (existing titles are
     * overwritten by the backup's data, new ones are added — nothing already tracked is removed),
     * then marks everything as pending so it also propagates to Firestore.
     *
     * The watch log is merged by [BackupWatchLogEntry.syncId] rather than upserted (it has no
     * natural primary key to upsert on) and, like a TV Time import, isn't individually re-pushed
     * to Firestore — pushing what can be thousands of entries one write at a time would be slow
     * and is unnecessary for a same-account restore. On a genuine migration to a fresh account,
     * this means the watch log stays local to this device until a new watch event re-triggers
     * sync — an existing, already-accepted limitation shared with TV Time import.
     */
    suspend fun import(backup: LibraryBackup): BackupImportReport = withContext(Dispatchers.IO) {
        val showEntities = backup.shows.map { it.toEntity() }
        showDao.upsertShows(showEntities)
        episodeDao.upsertEpisodes(backup.shows.flatMap { show -> show.episodes.map { it.toEntity(show.tmdbId) } })

        val movieEntities = backup.movies.map { it.toEntity() }
        movieDao.upsertMovies(movieEntities)

        val existingSyncIds = watchLogDao.getAllSyncIds().toSet()
        val newWatchLogEntries = backup.watchLog
            .filter { it.syncId !in existingSyncIds }
            .map { it.toEntity() }
        if (newWatchLogEntries.isNotEmpty()) watchLogDao.insertAll(newWatchLogEntries)

        val now = Instant.now()
        showEntities.forEach { syncOutboxDao.markPending(SyncOutboxEntity(it.tmdbId, MediaType.TV, now)) }
        movieEntities.forEach { syncOutboxDao.markPending(SyncOutboxEntity(it.tmdbId, MediaType.MOVIE, now)) }
        // Fire-and-forget — same rationale as TvTimeImportRepository: hundreds of sequential
        // Firestore writes shouldn't block this screen once the local import (the part the user
        // is actually waiting on) is done.
        firestoreSyncRepository.pushPendingChangesInBackground()

        BackupImportReport(
            showCount = showEntities.size,
            episodeCount = backup.shows.sumOf { it.episodes.size },
            movieCount = movieEntities.size,
            newWatchLogEntryCount = newWatchLogEntries.size,
            exportedAt = backup.exportedAt,
        )
    }
}
