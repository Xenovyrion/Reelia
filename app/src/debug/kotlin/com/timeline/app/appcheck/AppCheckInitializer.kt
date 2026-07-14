package com.timeline.app.appcheck

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/** Debug builds can't pass Play Integrity's attestation, so they use the Debug provider instead.
 * The first run prints a debug token in Logcat (tag "DebugAppCheckProviderFactory") — register it
 * under Firebase console > App Check > Apps > (this app) > Manage debug tokens so debug builds
 * keep working once App Check is enforced. */
fun installAppCheckProviderFactory() {
    Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
}
