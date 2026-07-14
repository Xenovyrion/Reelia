package com.timeline.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.timeline.app.appcheck.installAppCheckProviderFactory
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
        // Must run before any other Firebase Auth/Firestore call so every request already
        // carries an App Check token.
        installAppCheckProviderFactory()
        val languageCode = runBlocking(Dispatchers.IO) { languageStore.language.first() }
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(LanguagePreferenceStore.uiLocaleTagFor(languageCode)),
        )
    }
}
