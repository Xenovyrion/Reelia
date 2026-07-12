package com.timeline.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.timeline.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private const val RELEASE_TAG_URL =
    "https://api.github.com/repos/Xenovyrion/TimeLine/releases/tags/debug-latest"

/**
 * Checks GitHub's rolling "debug-latest" release for a build newer than this one, since Reelia
 * isn't distributed through the Play Store and gets no store-driven auto-updates. "Newer" is
 * decided by comparing the release's `target_commitish` (set by CI to the exact commit it built)
 * against this build's own [BuildConfig.GIT_SHA] — not by a version number, since every CI build
 * republishes the same rolling tag rather than incrementing one.
 */
@Singleton
class AppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(RELEASE_TAG_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val release = json.decodeFromString<GithubRelease>(body)
                if (release.target_commitish == BuildConfig.GIT_SHA) return@withContext null
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return@withContext null
                AppUpdate(
                    commitSha = release.target_commitish,
                    downloadUrl = apkAsset.browser_download_url,
                    releaseUrl = release.html_url,
                )
            }
        } catch (e: Exception) {
            // Best-effort background check — no network, GitHub down, rate-limited, whatever.
            // Never surfaced as an error; the next check (app restart or manual retry) tries again.
            null
        }
    }

    /** Downloads the APK into the app's cache dir and returns a content:// Uri the system
     * package installer can read (see the FileProvider declared in the manifest). */
    suspend fun downloadUpdate(update: AppUpdate): Uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
        val file = File(dir, "reelia-update.apk")
        val request = Request.Builder().url(update.downloadUrl).build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body ?: error("Empty APK download response")
            file.outputStream().use { output -> body.byteStream().copyTo(output) }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun buildInstallIntent(apkUri: Uri): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
