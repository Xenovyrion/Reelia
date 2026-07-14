package com.timeline.app.crash

import android.content.Context
import android.os.Build
import android.util.Log
import com.timeline.app.BuildConfig
import java.io.File

private const val CRASH_LOG_FILE = "last_crash.txt"

/** Installs a global uncaught-exception handler that persists the stack trace to internal
 * storage before letting the crash proceed normally — with no adb/Android Studio access on the
 * device, this is the only practical way to see what actually crashed. Read back and shown once
 * on the next launch via [readAndClearCrashLog]. */
fun installCrashLogger(context: Context) {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
            File(context.filesDir, CRASH_LOG_FILE).writeText(
                "Reelia ${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_SHA.take(7)}) — " +
                    "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}\n" +
                    "Thread: ${thread.name}\n\n" +
                    Log.getStackTraceString(throwable),
            )
        } catch (e: Exception) {
            // Best-effort — logging the crash must never interfere with the crash itself.
        }
        previousHandler?.uncaughtException(thread, throwable)
    }
}

/** Null if the app didn't crash last run. Deletes the file once read so it's only ever shown
 * once, the next time the app is launched after the crash happened. */
fun readAndClearCrashLog(context: Context): String? {
    val file = File(context.filesDir, CRASH_LOG_FILE)
    if (!file.exists()) return null
    val text = file.readText()
    file.delete()
    return text
}
