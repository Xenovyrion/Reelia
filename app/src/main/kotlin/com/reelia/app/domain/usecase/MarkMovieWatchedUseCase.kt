package com.reelia.app.domain.usecase

import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.WatchLogEntryEntity
import com.reelia.app.data.sync.FirestoreSyncRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class MarkMovieWatchedUseCase @Inject constructor(
    private val movieDao: MovieDao,
    private val watchLogDao: WatchLogDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) {
    suspend operator fun invoke(movieId: Int, watched: Boolean, watchedAt: Instant = Instant.now()) {
        val now = Instant.now()
        movieDao.setMovieWatched(movieId, watched, if (watched) watchedAt else null, now)
        // status otherwise only ever gets set once (PLAN_TO_WATCH) when the movie is added, and
        // never reflects the actual watched flag on its own.
        movieDao.setMovieStatus(movieId, if (watched) WatchStatus.COMPLETED else WatchStatus.PLAN_TO_WATCH, now)

        if (watched) {
            val movie = movieDao.getMovie(movieId).first() ?: return
            val entry = WatchLogEntryEntity(
                mediaType = MediaType.MOVIE,
                tmdbId = movieId,
                runtimeMinutes = movie.runtimeMinutes ?: 0,
                watchedAt = watchedAt,
            )
            watchLogDao.insert(entry)
            firestoreSyncRepository.pushWatchLogEntry(entry)
        }

        syncOutboxDao.markPending(SyncOutboxEntity(movieId, MediaType.MOVIE, now))
        firestoreSyncRepository.pushPendingChanges()
    }
}
