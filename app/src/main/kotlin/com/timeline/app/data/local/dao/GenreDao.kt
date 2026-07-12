package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.ShowGenreCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {
    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Upsert
    suspend fun upsertShowCrossRefs(crossRefs: List<ShowGenreCrossRef>)

    @Upsert
    suspend fun upsertMovieCrossRefs(crossRefs: List<MovieGenreCrossRef>)

    @Query(
        """
        SELECT DISTINCT g.* FROM genres g
        JOIN show_genre_cross_ref sg ON g.tmdbId = sg.genreId
        ORDER BY g.name
        """,
    )
    fun getGenresForTrackedShows(): Flow<List<GenreEntity>>

    @Query("SELECT * FROM show_genre_cross_ref")
    fun getAllShowGenreCrossRefs(): Flow<List<ShowGenreCrossRef>>

    @Query(
        """
        SELECT DISTINCT g.* FROM genres g
        JOIN movie_genre_cross_ref mg ON g.tmdbId = mg.genreId
        ORDER BY g.name
        """,
    )
    fun getGenresForTrackedMovies(): Flow<List<GenreEntity>>

    @Query("SELECT * FROM movie_genre_cross_ref")
    fun getAllMovieGenreCrossRefs(): Flow<List<MovieGenreCrossRef>>

    @Query(
        """
        SELECT g.* FROM genres g
        JOIN movie_genre_cross_ref mg ON g.tmdbId = mg.genreId
        WHERE mg.movieId = :movieId
        ORDER BY g.name
        """,
    )
    fun getGenresForMovie(movieId: Int): Flow<List<GenreEntity>>

    @Query(
        """
        SELECT g.* FROM genres g
        JOIN show_genre_cross_ref sg ON g.tmdbId = sg.genreId
        WHERE sg.showId = :showId
        ORDER BY g.name
        """,
    )
    fun getGenresForShow(showId: Int): Flow<List<GenreEntity>>
}
