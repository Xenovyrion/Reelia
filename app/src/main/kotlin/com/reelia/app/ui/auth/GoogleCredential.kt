package com.reelia.app.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.reelia.app.R

/** Shared by the sign-in screen and the account-deletion reauthentication dialog — both need the
 * same Credential Manager round trip to get a fresh Google ID token. */
suspend fun fetchGoogleIdToken(context: Context): String {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.google_web_client_id))
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    val result = CredentialManager.create(context).getCredential(context, request)
    return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
}
