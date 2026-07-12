package com.timeline.app.data.update

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val target_commitish: String,
    val html_url: String,
    val assets: List<GithubReleaseAsset> = emptyList(),
)

@Serializable
data class GithubReleaseAsset(
    val name: String,
    val browser_download_url: String,
)

/** A newer build than this one, detected on the rolling "debug-latest" GitHub release. */
data class AppUpdate(
    val commitSha: String,
    val downloadUrl: String,
    val releaseUrl: String,
)
