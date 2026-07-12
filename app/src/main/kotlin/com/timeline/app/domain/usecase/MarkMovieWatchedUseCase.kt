package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.SyncOutboxDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.SyncOutboxEntity
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.data.sync.FirestoreSyncRepository
import com.timeline.app.domain.model.MediaType
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

        if (watched) {
            val movie = movieDao.getMovie(movieId).first() ?: return
            watchLogDao.insert(
                WatchLogEntryEntity(
                    mediaType = MediaType.MOVIE,
                    tmdbId = movieId,
                    runtimeMinutes = movie.runtimeMinutes ?: 0,
                    watchedAt = watchedAt,
                ),
            )
        }

        syncOutboxDao.markPending(SyncOutboxEntity(movieId, MediaType.MOVIE, now))
        firestoreSyncRepository.pushPendingChanges()
    }
}
