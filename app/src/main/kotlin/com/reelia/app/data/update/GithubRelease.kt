package com.reelia.app.data.update

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val tag_name: String,
    val html_url: String,
    val assets: List<GithubReleaseAsset> = emptyList(),
)

@Serializable
data class GithubReleaseAsset(
    val name: String,
    val browser_download_url: String,
    /** GitHub-computed digest, e.g. "sha256:1f20d1ab...". Null only for assets uploaded before
     * GitHub started serving this (none of ours — every release asset here has one). */
    val digest: String? = null,
)

/** A newer version than this one, detected on GitHub's latest tagged release. */
data class AppUpdate(
    val versionName: String,
    val downloadUrl: String,
    val releaseUrl: String,
    /** Lowercase hex SHA-256, without the "sha256:" prefix. Verified against the downloaded
     * file's own hash before install is allowed to proceed — see AppUpdateRepository. */
    val expectedSha256: String?,
)
