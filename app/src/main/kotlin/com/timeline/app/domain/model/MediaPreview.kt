package com.timeline.app.domain.model

/**
 * Read-only, not-yet-tracked media info shown on the Search tab / preview screens.
 * Nothing here is persisted to Room unless the user taps "Ajouter" (which goes through
 * the normal ShowRepository/MovieRepository add flow instead).
 *
 * Feed items from [com.timeline.app.data.metadata.MetadataProvider.getTrendingFeed] only
 * populate the lightweight fields (title/poster/backdrop/overview/date/rating) — [cast],
 * [crew], [watchProviders], [genreNames] and [networkNames] are only filled in by
 * `getShowPreview`/`getMoviePreview`, which fetch the full detail+credits+watch-providers
 * trio for a single item.
 */
data class MediaPreview(
    val tmdbId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String?,
    val runtimeMinutes: Int? = null,
    val numberOfSeasons: Int? = null,
    val voteAverage: Float? = null,
    val genreNames: List<String> = emptyList(),
    val networkNames: List<String> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList(),
    val watchProviders: WatchProviders? = null,
)
