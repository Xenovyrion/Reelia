package com.reelia.app.ui.common.model

import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class UpcomingShowItem(
    val showId: Int,
    val showTitle: String,
    val episodeName: String,
    val networkNames: String?,
    val posterUrl: String?,
    val airDate: String,
    val daysUntil: Long,
)

data class UpcomingMovieItem(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String,
    val daysUntil: Long,
)

suspend fun buildUpcomingShowItems(
    shows: List<TrackedShowEntity>,
    imageUrlBuilder: TmdbImageUrlBuilder,
    today: LocalDate = LocalDate.now(),
): List<UpcomingShowItem> = shows
    .mapNotNull { show ->
        val airDateStr = show.nextEpisodeToAirDate ?: return@mapNotNull null
        val airDate = runCatching { LocalDate.parse(airDateStr) }.getOrNull() ?: return@mapNotNull null
        UpcomingShowItem(
            showId = show.tmdbId,
            showTitle = show.name,
            episodeName = show.nextEpisodeToAirName.orEmpty(),
            networkNames = show.networkNames,
            posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
            airDate = airDateStr,
            daysUntil = ChronoUnit.DAYS.between(today, airDate),
        )
    }
    .sortedBy { it.airDate }

/** Unlike shows (which surface an already-aired next episode as "Aired"), movies only ever
 * show future releases — a past [releaseDate] just means the movie is already out. */
suspend fun buildUpcomingMovieItems(
    movies: List<TrackedMovieEntity>,
    imageUrlBuilder: TmdbImageUrlBuilder,
    today: LocalDate = LocalDate.now(),
): List<UpcomingMovieItem> = movies
    .mapNotNull { movie ->
        val releaseDateStr = movie.releaseDate ?: return@mapNotNull null
        val releaseDate = runCatching { LocalDate.parse(releaseDateStr) }.getOrNull() ?: return@mapNotNull null
        if (releaseDate.isBefore(today)) return@mapNotNull null
        UpcomingMovieItem(
            movieId = movie.tmdbId,
            title = movie.title,
            posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
            releaseDate = releaseDateStr,
            daysUntil = ChronoUnit.DAYS.between(today, releaseDate),
        )
    }
    .sortedBy { it.releaseDate }
