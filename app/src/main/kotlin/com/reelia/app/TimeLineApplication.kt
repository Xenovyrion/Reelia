package com.reelia.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.reelia.app.appcheck.installAppCheckProviderFactory
import com.reelia.app.data.local.prefs.LanguagePreferenceStore
import com.reelia.app.data.notifications.ReleaseReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class TimeLineApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var languageStore: LanguagePreferenceStore

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Must run before any other Firebase Auth/Firestore call so every request already
        // carries an App Check token. Best-effort: App Check is a hardening layer, never a
        // reason the whole app should fail to start if the SDK/Play Services misbehave.
        try {
            installAppCheckProviderFactory()
        } catch (e: Exception) {
            Log.e("TimeLineApplication", "App Check provider install failed", e)
        }
        val languageCode = runBlocking(Dispatchers.IO) { languageStore.language.first() }
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(LanguagePreferenceStore.uiLocaleTagFor(languageCode)),
        )

        createNotificationChannel()
        scheduleReleaseReminderWork()
    }

    /** Unconditional — minSdk 26 already requires a channel for any notification to show at all,
     * so there's no version check to guard here. */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReleaseReminderWorker.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(R.string.notification_channel_description) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Cheap and idempotent to call on every app start — KEEP means an already-scheduled job is
     * left alone. The worker itself checks whether the user has actually enabled reminders and
     * no-ops immediately if not, so there's no need to enqueue/cancel this in lockstep with the
     * Settings toggle. */
    private fun scheduleReleaseReminderWork() {
        val request = PeriodicWorkRequestBuilder<ReleaseReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(ReleaseReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** A shared crossfading ImageLoader — without it, posters/backdrops pop in abruptly the
     * instant they finish decoding, which reads as jank while scrolling fast even when frame
     * timing itself is fine. Coil picks this up automatically for every AsyncImage in the app. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).crossfade(true).build()
}
