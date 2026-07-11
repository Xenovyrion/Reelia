package com.timeline.app.data.metadata

import com.timeline.app.domain.model.MediaPreview
import com.timeline.app.domain.model.TmdbSearchResult

/**
 * Seam for the metadata/search backend used by the Search tab and preview screens. Only TMDB
 * is wired up today; [isAvailable] lets Settings show other providers as disabled placeholders
 * without any of them being functional yet. Existing add/detail flows (ShowRepository,
 * MovieRepository, SearchRepository) intentionally do NOT go through this seam — only the
 * newer discovery surfaces do.
 */
interface MetadataProvider {
    val id: String
    val displayName: String
    val isAvailable: Boolean

    suspend fun search(query: String): List<TmdbSearchResult>
    suspend fun getTrendingFeed(): List<MediaPreview>
    suspend fun getShowPreview(tmdbId: Int): MediaPreview
    suspend fun getMoviePreview(tmdbId: Int): MediaPreview
}
