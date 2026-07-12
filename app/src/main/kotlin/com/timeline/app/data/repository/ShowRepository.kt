package com.timeline.app.data.repository

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.GenreDao
import com.timeline.app.data.local.dao.SeasonDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.ShowEpisodeProgress
import com.timeline.app.data.local.entity.EpisodeEntity
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.ShowGenreCrossRef
import com.timeline.app.data.local.entity.ShowWithDetails
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.mappers.toEntity
import com.timeline.app.data.remote.tmdb.mappers.toEpisodeEntities
import com.timeline.app.data.remote.tmdb.mappers.toGenreEntities
import com.timeline.app.data.remote.tmdb.mappers.toSeasonEntities
import com.timeline.app.domain.model.WatchStatus
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Singleton
class ShowRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val showDao: ShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val genreDao: GenreDao,
) {
    fun getAllShows(): Flow<List<TrackedShowEntity>> = showDao.getAllShows()

    fun getShowWithDetails(showId: Int): Flow<ShowWithDetails?> = showDao.getShowWithDetails(showId)

    fun getEpisodeProgressByShow(): Flow<List<ShowEpisodeProgress>> = episodeDao.getEpisodeProgressByShow()

    fun getAllUnwatchedEpisodesOrdered(): Flow<List<EpisodeEntity>> = episodeDao.getAllUnwatchedEpisodesOrdered()

    fun getGenresForTrackedShows(): Flow<List<GenreEntity>> = genreDao.getGenresForTrackedShows()

    fun getGenresForShow(showId: Int): Flow<List<GenreEntity>> = genreDao.getGenresForShow(showId)

    fun getShowGenreCrossRefs(): Flow<List<ShowGenreCrossRef>> = genreDao.getAllShowGenreCrossRefs()

    /** Fetches full show + every season's episodes from TMDB and persists it as a new tracked
     * show. All seasons are fetched concurrently so progress tracking is correct from the start
     * — a show is never left with only some seasons' episodes locally cached. */
    suspend fun addShowFromTmdb(tmdbId: Int): Unit = coroutineScope {
        val details = tmdbApi.getTvDetails(tmdbId)
        showDao.upsertShow(details.toEntity(status = WatchStatus.PLAN_TO_WATCH, addedAt = Instant.now()))
        seasonDao.upsertSeasons(details.toSeasonEntities())
        persistGenres(details.toGenreEntities(), tmdbId)

        val seasonNumbers = details.seasons.map { it.seasonNumber }.filter { it > 0 }
        val defaultRuntimeMinutes = details.episodeRunTime.firstOrNull()
        seasonNumbers
            .map { seasonNumber -> async { ensureSeasonEpisodesLoaded(tmdbId, seasonNumber, defaultRuntimeMinutes) } }
            .awaitAll()
    }

    /** Fetches and caches a season's episode list. Safe to call even if it's already loaded —
     * upsert is idempotent. */
    suspend fun ensureSeasonEpisodesLoaded(showId: Int, seasonNumber: Int, defaultRuntimeMinutes: Int?) {
        val seasonDetails = tmdbApi.getSeasonDetails(showId, seasonNumber)
        episodeDao.upsertEpisodes(seasonDetails.toEpisodeEntities(showId, defaultRuntimeMinutes))
    }

    private suspend fun persistGenres(genres: List<GenreEntity>, showId: Int) {
        if (genres.isEmpty()) return
        genreDao.upsertGenres(genres)
        genreDao.upsertShowCrossRefs(genres.map { ShowGenreCrossRef(showId = showId, genreId = it.tmdbId) })
    }
}
