package com.reelia.app.data.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        // Best-effort welcome/confirmation signal — Firebase's own built-in email, no backend
        // needed. Must never block account creation if it fails to send.
        runCatching { firebaseAuth.currentUser?.sendEmailVerification()?.await() }
    }

    suspend fun signInWithGoogleIdToken(idToken: String) {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    /** True once the account was created with email/password (as opposed to only Google) —
     * determines whether "confirm your identity" (see reauthenticateWithPassword) should prompt
     * for a password or for a fresh Google sign-in. */
    fun isPasswordAccount(user: FirebaseUser?): Boolean =
        user?.providerData.orEmpty().any { it.providerId == EmailAuthProvider.PROVIDER_ID }

    /** Firebase requires a "recent" sign-in before allowing sensitive operations like account
     * deletion — this re-proves identity without forcing a full sign-out/sign-in round trip. */
    suspend fun reauthenticateWithPassword(password: String) {
        val user = firebaseAuth.currentUser ?: error("Not signed in")
        val email = user.email ?: error("Account has no email")
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
    }

    suspend fun reauthenticateWithGoogleIdToken(idToken: String) {
        val user = firebaseAuth.currentUser ?: error("Not signed in")
        user.reauthenticate(GoogleAuthProvider.getCredential(idToken, null)).await()
    }
}
