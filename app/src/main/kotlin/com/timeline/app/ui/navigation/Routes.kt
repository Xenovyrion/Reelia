package com.timeline.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val LIBRARY_GENRE = "library_genre/{genreId}"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val SHOW_DETAIL = "show_detail/{showId}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val SHOW_PREVIEW = "show_preview/{tmdbId}"
    const val MOVIE_PREVIEW = "movie_preview/{tmdbId}"
    const val PERSON_DETAIL = "person_detail/{personId}"

    fun libraryGenre(genreId: Int) = "library_genre/$genreId"
    fun showDetail(showId: Int) = "show_detail/$showId"
    fun movieDetail(movieId: Int) = "movie_detail/$movieId"
    fun showPreview(tmdbId: Int) = "show_preview/$tmdbId"
    fun moviePreview(tmdbId: Int) = "movie_preview/$tmdbId"
    fun personDetail(personId: Int) = "person_detail/$personId"
}
