package com.timeline.app

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.timeline.app.appcheck.installAppCheckProviderFactory
import com.timeline.app.crash.installCrashLogger
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class TimeLineApplication : Application() {

    @Inject
    lateinit var languageStore: LanguagePreferenceStore

    override fun onCreate() {
        super.onCreate()
        // First thing, so it catches crashes from anything below too — there's no adb access
        // on the device this runs on, so this is the only way to see a real stack trace.
        installCrashLogger(this)
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
    }
}
