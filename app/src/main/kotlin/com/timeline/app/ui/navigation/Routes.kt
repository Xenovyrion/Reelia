package com.timeline.app.ui.navigation

object Routes {
    const val SERIES = "series"
    const val FILMS = "films"
    const val SEARCH = "search"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val SHOW_DETAIL = "show_detail/{showId}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val SHOW_PREVIEW = "show_preview/{tmdbId}"
    const val MOVIE_PREVIEW = "movie_preview/{tmdbId}"
    const val PERSON_DETAIL = "person_detail/{personId}"

    fun showDetail(showId: Int) = "show_detail/$showId"
    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
    fun showPreview(tmdbId: Int) = "show_preview/$tmdbId"
    fun moviePreview(tmdbId: Int) = "movie_preview/$tmdbId"
    fun personDetail(personId: Int) = "person_detail/$personId"
}
