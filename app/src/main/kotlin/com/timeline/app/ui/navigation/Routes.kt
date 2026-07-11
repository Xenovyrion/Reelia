package com.timeline.app.ui.navigation

object Routes {
    const val LIBRARY = "library"
    const val ADD_MEDIA = "add_media"
    const val SHOW_DETAIL = "show_detail/{showId}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val UP_NEXT = "up_next"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun showDetail(showId: Int) = "show_detail/$showId"
    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
}
