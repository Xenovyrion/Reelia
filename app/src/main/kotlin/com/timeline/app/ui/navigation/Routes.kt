package com.timeline.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val SHOW_DETAIL = "show_detail/{showId}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val SHOW_PREVIEW = "show_preview/{tmdbId}"
    const val MOVIE_PREVIEW = "movie_preview/{tmdbId}"
    const val PERSON_DETAIL = "person_detail/{personId}"
    const val TV_TIME_IMPORT = "tv_time_import"

    fun showDetail(showId: Int) = "show_detail/$showId"
    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
    fun showPreview(tmdbId: Int) = "show_preview/$tmdbId"
    fun moviePreview(tmdbId: Int) = "movie_preview/$tmdbId"
    fun personDetail(personId: Int) = "person_detail/$personId"
}
