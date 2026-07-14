package com.reelia.app.ui.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * A plain `ACTION_VIEW` intent lets Android route the URL to whatever app claims it via a
 * verified App Link — for gdpr.tvtime.com that can be the TV Time app itself, which defeats the
 * point of sending the user to a page explaining how to leave TV Time. Resolving the device's
 * default handler for a bare `http://` URL (no host, so app-specific link claims don't match it)
 * gives the actual default browser, which is then targeted explicitly so the app-link claim is
 * bypassed and the URL opens in a real browser tab, not the TV Time app or an in-app view.
 */
fun openInExternalBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val browserPackage = context.packageManager
        .resolveActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://")), PackageManager.MATCH_DEFAULT_ONLY)
        ?.activityInfo
        ?.packageName
    if (browserPackage != null) {
        intent.setPackage(browserPackage)
    }
    context.startActivity(intent)
}
