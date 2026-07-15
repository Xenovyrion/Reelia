package com.reelia.app.data.repository

import com.reelia.app.data.local.dao.GenreDao
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.entity.GenreEntity
import com.reelia.app.data.local.entity.MovieGenreCrossRef
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.remote.tmdb.TmdbApi
import com.reelia.app.data.remote.tmdb.mappers.toContentRating
import com.reelia.app.data.remote.tmdb.mappers.toEntity
import com.reelia.app.data.remote.tmdb.mappers.toGenreEntities
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class MovieRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val movieDao: MovieDao,
    private val genreDao: GenreDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    fun getAllMovies(): Flow<List<TrackedMovieEntity>> = movieDao.getAllMovies()

    fun getMovie(movieId: Int): Flow<TrackedMovieEntity?> = movieDao.getMovie(movieId)

    fun getGenresForTrackedMovies(): Flow<List<GenreEntity>> = genreDao.getGenresForTrackedMovies()

    fun getMovieGenreCrossRefs(): Flow<List<MovieGenreCrossRef>> = genreDao.getAllMovieGenreCrossRefs()

    fun getGenresForMovie(movieId: Int): Flow<List<GenreEntity>> = genreDao.getGenresForMovie(movieId)

    /** One-time backfill for movies whose `status` predates the fix that keeps it in sync with
     * the real `watched` flag (status used to only ever be set once, at add-time). Only writes
     * rows that are actually out of sync. */
    suspend fun reconcileAllStatuses() {
        val now = Instant.now()
        movieDao.getAllMovies().first().forEach { movie ->
            val computedStatus = if (movie.watched) WatchStatus.COMPLETED else WatchStatus.PLAN_TO_WATCH
            if (computedStatus != movie.status) {
                movieDao.setMovieStatus(movie.tmdbId, computedStatus, now)
            }
        }
    }

    suspend fun setFavorite(movieId: Int, isFavorite: Boolean) {
        val now = Instant.now()
        movieDao.setMovieFavorite(movieId, isFavorite, now)
        syncOutboxDao.markPending(SyncOutboxEntity(movieId, MediaType.MOVIE, now))
        firestoreSyncRepository.pushPendingChanges()
    }

    /** Removes a movie from the library and its Firestore document — see
     * [com.reelia.app.data.repository.ShowRepository.removeShow] for the equivalent on shows. */
    suspend fun removeMovie(movieId: Int) {
        val movie = movieDao.getMovieOnce(movieId) ?: return
        genreDao.deleteMovieCrossRefs(movieId)
        movieDao.deleteMovie(movie)
        syncOutboxDao.clearPending(movieId, MediaType.MOVIE)
        firestoreSyncRepository.deleteMovieRemote(movieId)
    }

    /** Fetches a movie from TMDB, persists it, then pushes its existence to Firestore so it can
     * sync to other devices. */
    suspend fun addMovieFromTmdb(tmdbId: Int) {
        fetchAndPersistFromTmdb(tmdbId)
        val now = Instant.now()
        syncOutboxDao.markPending(SyncOutboxEntity(tmdbId, MediaType.MOVIE, now))
        firestoreSyncRepository.pushPendingChanges()
    }

    /** Used by FirestoreSyncRepository when a movie is discovered remotely for the first time —
     * fetches TMDB metadata only, without pushing back (the caller applies the authoritative
     * remote personal-state right after). */
    suspend fun fetchAndPersistFromTmdb(tmdbId: Int): Unit = coroutineScope {
        val detailsDeferred = async { tmdbApi.getMovieDetails(tmdbId) }
        val contentRatingDeferred = async { runCatching { tmdbApi.getMovieReleaseDates(tmdbId) }.getOrNull()?.toContentRating() }
        val details = detailsDeferred.await()
        val contentRating = contentRatingDeferred.await()
        movieDao.upsertMovie(details.toEntity(status = WatchStatus.PLAN_TO_WATCH, addedAt = Instant.now(), contentRating = contentRating))
        persistGenres(details.toGenreEntities(), tmdbId)
    }

    private suspend fun persistGenres(genres: List<GenreEntity>, movieId: Int) {
        if (genres.isEmpty()) return
        genreDao.upsertGenres(genres)
        genreDao.upsertMovieCrossRefs(genres.map { MovieGenreCrossRef(movieId = movieId, genreId = it.tmdbId) })
    }
}
