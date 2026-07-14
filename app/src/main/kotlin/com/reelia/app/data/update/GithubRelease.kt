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
)

/** A newer version than this one, detected on GitHub's latest tagged release. */
data class AppUpdate(
    val versionName: String,
    val downloadUrl: String,
    val releaseUrl: String,
)
