package com.reelia.app.domain.model

/** Used only when TMDB has no runtime data at all for a title (neither a per-episode value
 * nor a show-level average) — logging such a watch as 0 minutes would silently undercount
 * total watched time despite the episode/movie definitely having *some* real duration. */
object RuntimeDefaults {
    const val DEFAULT_EPISODE_RUNTIME_MINUTES = 45
    const val DEFAULT_MOVIE_RUNTIME_MINUTES = 100
}
