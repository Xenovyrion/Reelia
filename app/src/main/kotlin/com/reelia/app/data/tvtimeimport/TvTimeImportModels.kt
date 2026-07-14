package com.reelia.app.data.tvtimeimport

import java.time.Instant

/** A show tracked in TV Time, keyed by its TheTVDB id (TV Time's `s_id`/`series_id`) — TMDB's
 * `/find` endpoint resolves this to an exact TMDB match, so shows never need fuzzy title search. */
data class TvTimeShowImport(
    val tvdbId: Int,
    val name: String,
    /** (seasonNumber, episodeNumber) -> when it was watched. */
    val watchedEpisodes: Map<Pair<Int, Int>, Instant>,
)

/** A movie tracked in TV Time. Movies carry no stable external id in the export, only a title +
 * release year, so matching against TMDB is a best-effort title/year search. */
data class TvTimeMovieImport(
    val name: String,
    val releaseYear: Int?,
    val watched: Boolean,
    val watchedAt: Instant?,
)

data class TvTimeImportData(
    val shows: List<TvTimeShowImport>,
    val movies: List<TvTimeMovieImport>,
)

data class TvTimeImportProgress(val done: Int, val total: Int)

data class TvTimeImportReport(
    val importedShowNames: List<String>,
    val importedEpisodeCount: Int,
    val importedMovieNames: List<String>,
    val unmatchedShowNames: List<String>,
    val unmatchedMovieNames: List<String>,
) {
    val importedShowCount: Int get() = importedShowNames.size
    val importedMovieCount: Int get() = importedMovieNames.size
}
