package com.timeline.app.domain.usecase

import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class MarkMovieWatchedUseCase @Inject constructor(
    private val movieDao: MovieDao,
    private val watchLogDao: WatchLogDao,
) {
    suspend operator fun invoke(movieId: Int, watched: Boolean, watchedAt: Instant = Instant.now()) {
        movieDao.setMovieWatched(movieId, watched, if (watched) watchedAt else null)

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
    }
}
