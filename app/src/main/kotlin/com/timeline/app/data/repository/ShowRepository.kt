package com.timeline.app.data.repository

import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.GenreDao
import com.timeline.app.data.local.dao.SeasonDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.ShowEpisodeProgress
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
import kotlinx.coroutines.flow.Flow

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

    /** Fetches full show + season 1 metadata from TMDB and persists it as a new tracked show. */
    suspend fun addShowFromTmdb(tmdbId: Int) {
        val details = tmdbApi.getTvDetails(tmdbId)
        showDao.upsertShow(details.toEntity(status = WatchStatus.PLAN_TO_WATCH, addedAt = Instant.now()))
        seasonDao.upsertSeasons(details.toSeasonEntities())
        persistGenres(details.toGenreEntities(), tmdbId)

        val firstSeasonNumber = details.seasons.firstOrNull { it.seasonNumber > 0 }?.seasonNumber
        if (firstSeasonNumber != null) {
            val seasonDetails = tmdbApi.getSeasonDetails(tmdbId, firstSeasonNumber)
            episodeDao.upsertEpisodes(
                seasonDetails.toEpisodeEntities(tmdbId, details.episodeRunTime.firstOrNull()),
            )
        }
    }

    /** Fetches and caches the episode list for a season that hasn't been loaded yet. */
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
