package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.timeline.app.data.local.entity.TrackedMovieEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM tracked_movies ORDER BY addedAt DESC")
    fun getAllMovies(): Flow<List<TrackedMovieEntity>>

    @Query("SELECT * FROM tracked_movies WHERE tmdbId = :movieId")
    fun getMovie(movieId: Int): Flow<TrackedMovieEntity?>

    @Query("SELECT * FROM tracked_movies WHERE tmdbId = :movieId")
    suspend fun getMovieOnce(movieId: Int): TrackedMovieEntity?

    @Upsert
    suspend fun upsertMovie(movie: TrackedMovieEntity)

    @Query("UPDATE tracked_movies SET watched = :watched, watchedAt = :watchedAt WHERE tmdbId = :movieId")
    suspend fun setMovieWatched(movieId: Int, watched: Boolean, watchedAt: Instant?)

    @Query("UPDATE tracked_movies SET isFavorite = :isFavorite, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :movieId")
    suspend fun setMovieFavorite(movieId: Int, isFavorite: Boolean, lastModifiedAt: Instant)

    @Delete
    suspend fun deleteMovie(movie: TrackedMovieEntity)
}
