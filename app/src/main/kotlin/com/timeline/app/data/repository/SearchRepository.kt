package com.timeline.app.data.repository

import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.dto.TmdbSearchResultDto
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.TmdbSearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
) {
    suspend fun searchMulti(query: String): List<TmdbSearchResult> {
        if (query.isBlank()) return emptyList()
        return tmdbApi.searchMulti(query).results.mapNotNull { it.toDomainOrNull() }
    }

    private fun TmdbSearchResultDto.toDomainOrNull(): TmdbSearchResult? {
        val type = when (mediaType) {
            "tv" -> MediaType.TV
            "movie" -> MediaType.MOVIE
            else -> return null
        }
        val resolvedTitle = (if (type == MediaType.TV) name else title) ?: return null
        return TmdbSearchResult(
            id = id,
            mediaType = type,
            title = resolvedTitle,
            posterPath = posterPath,
            overview = overview.orEmpty(),
            date = if (type == MediaType.TV) firstAirDate else releaseDate,
            genreIds = genreIds,
        )
    }
}
