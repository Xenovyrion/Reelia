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

private const val LATEST_RELEASE_URL =
    "https://api.github.com/repos/Xenovyrion/reelia-content/releases/latest"

/**
 * Checks GitHub's latest tagged release (e.g. `v0.13.0`) for a version newer than this build's
 * own [BuildConfig.VERSION_NAME], since Reelia isn't distributed through the Play Store and gets
 * no store-driven auto-updates. `/releases/latest` only ever returns the newest non-prerelease,
 * non-draft release, so the rolling "debug-latest" dogfooding build (always published as a
 * prerelease) never shows up here — only real version bumps do.
 */
@Singleton
class AppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(LATEST_RELEASE_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val release = json.decodeFromString<GithubRelease>(body)
                val remoteVersion = release.tag_name.removePrefix("v")
                if (!isNewerVersion(remoteVersion, BuildConfig.VERSION_NAME)) return@withContext null
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return@withContext null
                AppUpdate(
                    versionName = remoteVersion,
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

    /** Component-wise comparison ("0.9.0" < "0.13.0") rather than a string/lexicographic
     * compare, which would wrongly rank "0.13.0" below "0.9.0" ('1' < '9'). */
    private fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(remoteParts.size, localParts.size)) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    /** Downloads the APK into the app's cache dir and returns a content:// Uri the system
     * package installer can read (see the FileProvider declared in the manifest). Validates
     * the HTTP response before writing — without this check, a non-2xx response (e.g. GitHub
     * rate-limited or the asset briefly unavailable mid-publish) would silently write an error
     * page to "reelia-update.apk" and hand it to the installer, which then fails with Android's
     * generic, unhelpful "problem with the app file" dialog instead of a clear error here. */
    suspend fun downloadUpdate(update: AppUpdate): Uri = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
        val file = File(dir, "reelia-update.apk")
        val request = Request.Builder().url(update.downloadUrl).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Download failed: HTTP ${response.code}")
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
