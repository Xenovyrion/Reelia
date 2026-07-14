package com.reelia.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val tmdbId: Int,
    val name: String,
)

@Entity(tableName = "show_genre_cross_ref", primaryKeys = ["showId", "genreId"])
data class ShowGenreCrossRef(
    val showId: Int,
    val genreId: Int,
)

@Entity(tableName = "movie_genre_cross_ref", primaryKeys = ["movieId", "genreId"])
data class MovieGenreCrossRef(
    val movieId: Int,
    val genreId: Int,
)
