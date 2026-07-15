package com.reelia.app.domain.model

/** A user-selectable feed category for Home's per-type (movies/shows) discovery section.
 * [UPCOMING] maps to TMDB's "upcoming" for movies and "on the air" for shows; [NOW] maps to
 * "now playing" for movies and "airing today" for shows — same concept, different TMDB endpoint
 * per media type. */
enum class DiscoverCategory {
    POPULAR,
    TOP_RATED,
    UPCOMING,
    NOW,
}
