package com.reelia.app.appcheck

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/** Release builds attest via Play Integrity — this needs the app's release signing
 * certificate's SHA-256 fingerprint registered on the linked Google Cloud project (see
 * Firebase console > Project settings > Your apps, and Play Console > Setup > App integrity
 * once the app has a Play Store listing). */
fun installAppCheckProviderFactory() {
    Firebase.appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
}
