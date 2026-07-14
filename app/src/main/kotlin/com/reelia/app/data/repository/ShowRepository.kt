package com.reelia.app.data.repository

import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.EpisodeNameRow
import com.reelia.app.data.local.dao.GenreDao
import com.reelia.app.data.local.dao.SeasonDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.ShowEpisodeProgress
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.GenreEntity
import com.reelia.app.data.local.entity.ShowGenreCrossRef
import com.reelia.app.data.local.entity.ShowWithDetails
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.remote.tmdb.TmdbApi
import com.reelia.app.data.remote.tmdb.mappers.toEntity
import com.reelia.app.data.remote.tmdb.mappers.toEpisodeEntities
import com.reelia.app.data.remote.tmdb.mappers.toGenreEntities
import com.reelia.app.data.remote.tmdb.mappers.toSeasonEntities
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Singleton
class ShowRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val showDao: ShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val genreDao: GenreDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    fun getAllShows(): Flow<List<TrackedShowEntity>> = showDao.getAllShows()

    fun getShowWithDetails(showId: Int): Flow<ShowWithDetails?> = showDao.getShowWithDetails(showId)

    fun getEpisodeProgressByShow(): Flow<List<ShowEpisodeProgress>> = episodeDao.getEpisodeProgressByShow()

    fun getAllUnwatchedEpisodesOrdered(): Flow<List<EpisodeEntity>> = episodeDao.getAllUnwatchedEpisodesOrdered()

    fun getAllEpisodeNames(): Flow<List<EpisodeNameRow>> = episodeDao.getAllEpisodeNames()

    fun getGenresForTrackedShows(): Flow<List<GenreEntity>> = genreDao.getGenresForTrackedShows()

    fun getGenresForShow(showId: Int): Flow<List<GenreEntity>> = genreDao.getGenresForShow(showId)

    fun getShowGenreCrossRefs(): Flow<List<ShowGenreCrossRef>> = genreDao.getAllShowGenreCrossRefs()

    /** One-time backfill for shows whose `status` predates the fix that keeps it in sync with
     * real episode-watch progress (status used to only ever be set once, at add-time). Only
     * writes rows that are actually out of sync. */
    suspend fun reconcileAllStatuses() {
        val now = Instant.now()
        val progressByShowId = episodeDao.getEpisodeProgressByShow().first().associateBy { it.showId }
        showDao.getAllShows().first().forEach { show ->
            val progress = progressByShowId[show.tmdbId] ?: return@forEach
            if (progress.total == 0) return@forEach
            val computedStatus = when {
                progress.watchedCount == progress.total -> WatchStatus.COMPLETED
                progress.watchedCount > 0 -> WatchStatus.WATCHING
                else -> WatchStatus.PLAN_TO_WATCH
            }
            if (computedStatus != show.status) {
                showDao.setShowStatus(show.tmdbId, computedStatus, now)
            }
        }
    }

    suspend fun setFavorite(showId: Int, isFavorite: Boolean) {
        val now = Instant.now()
        showDao.setShowFavorite(showId, isFavorite, now)
        syncOutboxDao.markPending(SyncOutboxEntity(showId, MediaType.TV, now))
        firestoreSyncRepository.pushPendingChanges()
    }

    /** Removes a show from the library — episodes/seasons cascade-delete via their foreign key,
     * genre cross-refs are cleared explicitly (no FK on those), and the Firestore document is
     * deleted too so it doesn't come back down on the next sync. The watch log is untouched, so
     * past viewing still counts toward stats. */
    suspend fun removeShow(showId: Int) {
        val show = showDao.getShowOnce(showId) ?: return
        genreDao.deleteShowCrossRefs(showId)
        showDao.deleteShow(show)
        syncOutboxDao.clearPending(showId, MediaType.TV)
        firestoreSyncRepository.deleteShowRemote(showId)
    }

    /** Fetches full show + every season's episodes from TMDB and persists it as a new tracked
     * show, then pushes its existence to Firestore so it can sync to other devices. All seasons
     * are fetched concurrently so progress tracking is correct from the start — a show is never
     * left with only some seasons' episodes locally cached. */
    suspend fun addShowFromTmdb(tmdbId: Int) {
        fetchAndPersistFromTmdb(tmdbId)
        val now = Instant.now()
        syncOutboxDao.markPending(SyncOutboxEntity(tmdbId, MediaType.TV, now))
        firestoreSyncRepository.pushPendingChanges()
    }

    /** Used by FirestoreSyncRepository when a show is discovered remotely for the first time —
     * fetches TMDB metadata only, without pushing back to Firestore (the caller applies the
     * authoritative remote personal-state right after, so there's nothing new to push yet). */
    suspend fun fetchAndPersistFromTmdb(tmdbId: Int): Unit = coroutineScope {
        val details = tmdbApi.getTvDetails(tmdbId)
        showDao.upsertShow(details.toEntity(status = WatchStatus.PLAN_TO_WATCH, addedAt = Instant.now()))
        seasonDao.upsertSeasons(details.toSeasonEntities())
        persistGenres(details.toGenreEntities(), tmdbId)

        // Includes season 0 ("Specials") — TMDB uses it for extras like making-ofs, which the
        // user can track like any other episode. It's excluded from completion/progress math
        // (see EpisodeDao's progress queries) so a special left unwatched never blocks a show
        // from reading as 100% watched.
        val seasonNumbers = details.seasons.map { it.seasonNumber }
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
