package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.domain.model.WatchStatus
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

    @Query(
        "UPDATE tracked_movies SET watched = :watched, watchedAt = :watchedAt, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :movieId",
    )
    suspend fun setMovieWatched(movieId: Int, watched: Boolean, watchedAt: Instant?, lastModifiedAt: Instant)

    @Query("UPDATE tracked_movies SET isFavorite = :isFavorite, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :movieId")
    suspend fun setMovieFavorite(movieId: Int, isFavorite: Boolean, lastModifiedAt: Instant)

    @Query("UPDATE tracked_movies SET status = :status, lastModifiedAt = :lastModifiedAt WHERE tmdbId = :movieId")
    suspend fun setMovieStatus(movieId: Int, status: WatchStatus, lastModifiedAt: Instant)

    @Delete
    suspend fun deleteMovie(movie: TrackedMovieEntity)
}
