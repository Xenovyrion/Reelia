package com.timeline.app.ui.navigation

import android.net.Uri

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
    const val RELEASE_NOTES = "release_notes"
    const val STATS_DETAIL = "stats_detail/{filterType}/{filterId}/{filterLabel}"

    fun showDetail(showId: Int) = "show_detail/$showId"
    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
    fun showPreview(tmdbId: Int) = "show_preview/$tmdbId"
    fun moviePreview(tmdbId: Int) = "movie_preview/$tmdbId"
    fun personDetail(personId: Int) = "person_detail/$personId"
    fun statsDetail(filterType: String, filterId: String, filterLabel: String) =
        "stats_detail/$filterType/${Uri.encode(filterId)}/${Uri.encode(filterLabel)}"
}
