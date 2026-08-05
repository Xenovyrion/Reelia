package com.reelia.app.data.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.reelia.app.MainActivity
import com.reelia.app.R
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.NotifiedReminderDao
import com.reelia.app.data.local.dao.NotifiedReminderKey
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.entity.NotifiedReminderEntity
import com.reelia.app.data.local.prefs.NotificationPreferenceStore
import com.reelia.app.data.local.prefs.TmdbApiKeyStore
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.common.format.daysUntilOrNull
import com.reelia.app.ui.widget.createReeliaWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Runs ~daily (see enqueueing in [com.reelia.app.TimeLineApplication]). Best-effort, like every
 * other background sync path in this app (see AppUpdateRepository, FirestoreSyncRepository's
 * listeners) — no-ops rather than surfacing an error the user has no way to act on.
 *
 * Two responsibilities, in order:
 * 1. Refresh next-episode/release dates for titles whose date is unknown or already past — these
 *    are otherwise never refreshed after a title is first added (see `ShowDao.getShowsNeedingReleaseRefresh`),
 *    so without this step reminders would silently be computed from stale data.
 * 2. Fire a notification for every (title, enabled offset) pair whose countdown lands exactly on
 *    that offset today, that hasn't already been notified for this specific target date.
 */
@HiltWorker
class ReleaseReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationPreferenceStore: NotificationPreferenceStore,
    private val tmdbApiKeyStore: TmdbApiKeyStore,
    private val showDao: ShowDao,
    private val movieDao: MovieDao,
    private val notifiedReminderDao: NotifiedReminderDao,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
) : CoroutineWorker(context, workerParams) {

    private data class DueReminder(
        val mediaType: MediaType,
        val tmdbId: Int,
        val title: String,
        val offsetDays: Int,
        val targetDate: String,
    )

    override suspend fun doWork(): Result = runCatching {
        if (!notificationPreferenceStore.notificationsEnabled.first()) return@runCatching Result.success()
        if (tmdbApiKeyStore.currentKey.isNullOrBlank()) return@runCatching Result.success()

        val today = LocalDate.now()
        val todayStr = today.toString()
        refreshStaleDates(todayStr)

        val enabledOffsets = notificationPreferenceStore.enabledOffsets.first()
        val alreadyNotified = notifiedReminderDao.getAllKeys().toSet()

        val dueShows = showDao.getAllShows().first().mapNotNull { show ->
            val date = show.nextEpisodeToAirDate ?: return@mapNotNull null
            val daysUntil = date.daysUntilOrNull(today) ?: return@mapNotNull null
            dueReminderOrNull(MediaType.TV, show.tmdbId, show.name, date, daysUntil.toInt(), enabledOffsets, alreadyNotified)
        }
        val dueMovies = movieDao.getAllMovies().first().mapNotNull { movie ->
            val date = movie.releaseDate ?: return@mapNotNull null
            val daysUntil = date.daysUntilOrNull(today) ?: return@mapNotNull null
            dueReminderOrNull(MediaType.MOVIE, movie.tmdbId, movie.title, date, daysUntil.toInt(), enabledOffsets, alreadyNotified)
        }
        val due = dueShows + dueMovies

        if (due.isNotEmpty()) {
            postNotification(due)
            notifiedReminderDao.insertAll(
                due.map {
                    NotifiedReminderEntity(
                        tmdbId = it.tmdbId,
                        mediaType = it.mediaType,
                        offsetDays = it.offsetDays,
                        targetDate = it.targetDate,
                        notifiedAt = Instant.now(),
                    )
                },
            )
        }

        // Same refresh pass feeds both consumers: the notifications above, and the widget's
        // "what's coming up" list — no separate periodic work needed for the widget.
        runCatching { createReeliaWidget(applicationContext).updateAll(applicationContext) }

        Result.success()
    }.getOrElse { Result.success() }

    private fun dueReminderOrNull(
        mediaType: MediaType,
        tmdbId: Int,
        title: String,
        targetDate: String,
        daysUntil: Int,
        enabledOffsets: Set<Int>,
        alreadyNotified: Set<NotifiedReminderKey>,
    ): DueReminder? {
        if (daysUntil !in enabledOffsets) return null
        val key = NotifiedReminderKey(tmdbId, mediaType, daysUntil, targetDate)
        if (key in alreadyNotified) return null
        return DueReminder(mediaType, tmdbId, title, daysUntil, targetDate)
    }

    /** Bounded concurrency (6 in flight) — same pattern as TvTimeImportRepository.import(), since
     * a large library could otherwise fire dozens of simultaneous TMDB requests. Per-item
     * failures are swallowed so one bad title doesn't block the rest. */
    private suspend fun refreshStaleDates(today: String) = coroutineScope {
        val semaphore = Semaphore(permits = 6)
        val showJobs = showDao.getShowsNeedingReleaseRefresh(today).map { show ->
            async { semaphore.withPermit { runCatching { showRepository.fetchAndPersistFromTmdb(show.tmdbId) } } }
        }
        val movieJobs = movieDao.getMoviesNeedingReleaseRefresh(today).map { movie ->
            async { semaphore.withPermit { runCatching { movieRepository.fetchAndPersistFromTmdb(movie.tmdbId) } } }
        }
        (showJobs + movieJobs).awaitAll()
    }

    private fun postNotification(due: List<DueReminder>) {
        val context = applicationContext
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (due.size == 1) {
                putExtra(MainActivity.EXTRA_MEDIA_TYPE, due[0].mediaType.name)
                putExtra(MainActivity.EXTRA_TMDB_ID, due[0].tmdbId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = if (due.size == 1) {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_single_title))
                .setContentText(due[0].title)
        } else {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_digest_title, due.size))
                .setStyle(
                    NotificationCompat.InboxStyle().also { style ->
                        due.forEach { style.addLine(it.title) }
                    },
                )
        }
        builder
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build()) }
    }

    companion object {
        const val CHANNEL_ID = "release_reminders"
        const val WORK_NAME = "release_reminder_check"
        private const val NOTIFICATION_ID = 1001
    }
}
