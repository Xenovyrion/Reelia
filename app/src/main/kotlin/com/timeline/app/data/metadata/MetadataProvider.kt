package com.timeline.app.data.metadata

import com.timeline.app.domain.model.Genre
import com.timeline.app.domain.model.MediaPreview
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.TmdbSearchResult

/**
 * Seam for the metadata/search backend used by the Search tab, preview screens, and the Home
 * hub's discovery rows. Only TMDB is wired up today; [isAvailable] lets Settings show other
 * providers as disabled placeholders without any of them being functional yet. Existing
 * add/detail flows (ShowRepository, MovieRepository, SearchRepository) intentionally do NOT go
 * through this seam — only the newer discovery surfaces do.
 */
interface MetadataProvider {
    val id: String
    val displayName: String
    val isAvailable: Boolean

    suspend fun search(query: String): List<TmdbSearchResult>
    /** [mediaType] narrows the genre list to just movies or just TV; null returns the merged
     * list (used when search isn't locked to one type, e.g. Home's search entry point). */
    suspend fun getGenres(mediaType: MediaType?): List<Genre>
    suspend fun getTrendingFeed(): List<MediaPreview>
    suspend fun getRecentMoviesFeed(): List<MediaPreview>
    suspend fun getRecentShowsFeed(): List<MediaPreview>
    suspend fun getRecommendationsFeed(mediaType: MediaType, tmdbId: Int): List<MediaPreview>
    suspend fun getShowPreview(tmdbId: Int): MediaPreview
    suspend fun getMoviePreview(tmdbId: Int): MediaPreview
}
