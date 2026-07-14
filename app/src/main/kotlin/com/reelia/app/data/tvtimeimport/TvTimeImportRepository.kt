package com.reelia.app.data.tvtimeimport

import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.WatchLogEntryEntity
import com.reelia.app.data.remote.tmdb.TmdbApi
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.domain.usecase.refreshComputedStatus
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Matches TV Time export data against TMDB and persists it through the same repositories/DAOs
 * regular add/mark-watched flows use, so every existing derived-state mechanism (computed
 * status, sync outbox) stays consistent. Network calls run with bounded concurrency — a personal
 * library can mean a couple thousand TMDB requests, sequential would take too long but unbounded
 * parallelism would hammer the API.
 */
@Singleton
class TvTimeImportRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val showDao: ShowDao,
    private val movieDao: MovieDao,
    private val episodeDao: EpisodeDao,
    private val watchLogDao: WatchLogDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    suspend fun import(data: TvTimeImportData, onProgress: (TvTimeImportProgress) -> Unit): TvTimeImportReport {
        val total = data.shows.size + data.movies.size
        val done = AtomicInteger(0)
        val importedShowNames = CopyOnWriteArrayList<String>()
        val importedEpisodeCount = AtomicInteger(0)
        val importedMovieNames = CopyOnWriteArrayList<String>()
        val unmatchedShowNames = CopyOnWriteArrayList<String>()
        val unmatchedMovieNames = CopyOnWriteArrayList<String>()
        val semaphore = Semaphore(permits = 6)

        fun reportProgress() = onProgress(TvTimeImportProgress(done.incrementAndGet(), total))

        coroutineScope {
            val showJobs = data.shows.map { show ->
                async {
                    semaphore.withPermit {
                        runCatching { importShow(show, importedEpisodeCount) }
                            .onSuccess { matched -> if (matched) importedShowNames.add(show.name) else unmatchedShowNames.add(show.name) }
                            .onFailure { unmatchedShowNames.add(show.name) }
                    }
                    reportProgress()
                }
            }
            val movieJobs = data.movies.map { movie ->
                async {
                    semaphore.withPermit {
                        runCatching { importMovie(movie) }
                            .onSuccess { matched -> if (matched) importedMovieNames.add(movie.name) else unmatchedMovieNames.add(movie.name) }
                            .onFailure { unmatchedMovieNames.add(movie.name) }
                    }
                    reportProgress()
                }
            }
            (showJobs + movieJobs).awaitAll()
        }

        // Fire-and-forget: hundreds of sequential Firestore writes would otherwise hang this
        // screen well after the local import (the part the user is actually waiting on) is done.
        firestoreSyncRepository.pushPendingChangesInBackground()

        return TvTimeImportReport(
            importedShowNames = importedShowNames.sorted(),
            importedEpisodeCount = importedEpisodeCount.get(),
            importedMovieNames = importedMovieNames.sorted(),
            unmatchedShowNames = unmatchedShowNames.sorted(),
            unmatchedMovieNames = unmatchedMovieNames.sorted(),
        )
    }

    /** Returns true if the show was matched on TMDB (regardless of whether it was already tracked
     * locally — episodes are still applied on top of an existing show). */
    private suspend fun importShow(show: TvTimeShowImport, importedEpisodeCount: AtomicInteger): Boolean {
        val tmdbId = tmdbApi.findByExternalId(show.tvdbId).tvResults.firstOrNull()?.id ?: return false

        if (showDao.getShowOnce(tmdbId) == null) {
            showRepository.fetchAndPersistFromTmdb(tmdbId)
        }

        if (show.watchedEpisodes.isNotEmpty()) {
            val episodes = episodeDao.getEpisodesForShowOnce(tmdbId)
            val toMarkWatched = episodes.mapNotNull { episode ->
                if (episode.watched) return@mapNotNull null
                val watchedAt = show.watchedEpisodes[episode.seasonNumber to episode.episodeNumber] ?: return@mapNotNull null
                episode.copy(watched = true, watchedAt = watchedAt)
            }
            if (toMarkWatched.isNotEmpty()) {
                episodeDao.upsertEpisodes(toMarkWatched)
                watchLogDao.insertAll(
                    toMarkWatched.map { episode ->
                        WatchLogEntryEntity(
                            mediaType = MediaType.TV,
                            tmdbId = tmdbId,
                            seasonNumber = episode.seasonNumber,
                            episodeNumber = episode.episodeNumber,
                            runtimeMinutes = episode.runtimeMinutes ?: 0,
                            watchedAt = episode.watchedAt ?: Instant.now(),
                        )
                    },
                )
                importedEpisodeCount.addAndGet(toMarkWatched.size)
            }
        }

        val now = Instant.now()
        refreshComputedStatus(episodeDao, showDao, tmdbId, now)
        showDao.touchLastModified(tmdbId, now)
        syncOutboxDao.markPending(SyncOutboxEntity(tmdbId, MediaType.TV, now))
        return true
    }

    /** Returns true if the movie was matched on TMDB. */
    private suspend fun importMovie(movie: TvTimeMovieImport): Boolean {
        val results = tmdbApi.searchMovies(movie.name).results
        val best = results.firstOrNull { candidate ->
            movie.releaseYear != null && candidate.releaseDate?.take(4)?.toIntOrNull() == movie.releaseYear
        } ?: results.firstOrNull() ?: return false
        val tmdbId = best.id

        if (movieDao.getMovieOnce(tmdbId) == null) {
            movieRepository.fetchAndPersistFromTmdb(tmdbId)
        }

        val current = movieDao.getMovieOnce(tmdbId)
        val now = Instant.now()
        if (movie.watched && current?.watched != true) {
            val watchedAt = movie.watchedAt ?: now
            movieDao.setMovieWatched(tmdbId, true, watchedAt, now)
            watchLogDao.insertAll(
                listOf(
                    WatchLogEntryEntity(
                        mediaType = MediaType.MOVIE,
                        tmdbId = tmdbId,
                        runtimeMinutes = current?.runtimeMinutes ?: 0,
                        watchedAt = watchedAt,
                    ),
                ),
            )
        }
        val computedStatus = if (movie.watched || current?.watched == true) WatchStatus.COMPLETED else WatchStatus.PLAN_TO_WATCH
        movieDao.setMovieStatus(tmdbId, computedStatus, now)
        syncOutboxDao.markPending(SyncOutboxEntity(tmdbId, MediaType.MOVIE, now))
        return true
    }
}
