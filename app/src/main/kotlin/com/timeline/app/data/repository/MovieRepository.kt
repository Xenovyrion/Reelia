package com.timeline.app.data.repository

import com.timeline.app.data.local.dao.GenreDao
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.mappers.toEntity
import com.timeline.app.data.remote.tmdb.mappers.toGenreEntities
import com.timeline.app.domain.model.WatchStatus
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class MovieRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val movieDao: MovieDao,
    private val genreDao: GenreDao,
) {
    fun getAllMovies(): Flow<List<TrackedMovieEntity>> = movieDao.getAllMovies()

    fun getMovie(movieId: Int): Flow<TrackedMovieEntity?> = movieDao.getMovie(movieId)

    fun getGenresForTrackedMovies(): Flow<List<GenreEntity>> = genreDao.getGenresForTrackedMovies()

    fun getMovieGenreCrossRefs(): Flow<List<MovieGenreCrossRef>> = genreDao.getAllMovieGenreCrossRefs()

    suspend fun addMovieFromTmdb(tmdbId: Int) {
        val details = tmdbApi.getMovieDetails(tmdbId)
        movieDao.upsertMovie(details.toEntity(status = WatchStatus.PLAN_TO_WATCH, addedAt = Instant.now()))
        persistGenres(details.toGenreEntities(), tmdbId)
    }

    private suspend fun persistGenres(genres: List<GenreEntity>, movieId: Int) {
        if (genres.isEmpty()) return
        genreDao.upsertGenres(genres)
        genreDao.upsertMovieCrossRefs(genres.map { MovieGenreCrossRef(movieId = movieId, genreId = it.tmdbId) })
    }
}
