package com.timeline.app.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.SyncOutboxDao
import com.timeline.app.domain.model.MediaType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * MVP cross-device sync: pushes/pulls only the `isFavorite` flag for tracked shows and
 * movies, resolved last-write-wins by `lastModifiedAt`. Deliberately does not yet cover
 * per-episode/movie watched-state or the watch log — see Pass 8 notes for why this is
 * scoped down (those need a much larger, harder-to-verify sync surface).
 *
 * A remote favorite only applies to a title that already exists locally on this device —
 * this does not fetch new titles from TMDB on the other device's behalf.
 */
@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val syncOutboxDao: SyncOutboxDao,
    private val showDao: ShowDao,
    private val movieDao: MovieDao,
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var showsListener: ListenerRegistration? = null
    private var moviesListener: ListenerRegistration? = null

    private val _lastSyncedAt = MutableStateFlow<Instant?>(null)
    val lastSyncedAt: StateFlow<Instant?> = _lastSyncedAt.asStateFlow()

    suspend fun pushPendingChanges() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        for (entry in syncOutboxDao.getAllPending()) {
            when (entry.mediaType) {
                MediaType.TV -> {
                    val show = showDao.getShowOnce(entry.tmdbId) ?: continue
                    firestore.collection("users/$uid/shows").document(entry.tmdbId.toString())
                        .set(
                            mapOf(
                                "isFavorite" to show.isFavorite,
                                "lastModifiedAt" to show.lastModifiedAt.toEpochMilli(),
                            ),
                        ).await()
                }
                MediaType.MOVIE -> {
                    val movie = movieDao.getMovieOnce(entry.tmdbId) ?: continue
                    firestore.collection("users/$uid/movies").document(entry.tmdbId.toString())
                        .set(
                            mapOf(
                                "isFavorite" to movie.isFavorite,
                                "lastModifiedAt" to movie.lastModifiedAt.toEpochMilli(),
                            ),
                        ).await()
                }
            }
            syncOutboxDao.clearPending(entry.tmdbId, entry.mediaType)
        }
        _lastSyncedAt.value = Instant.now()
    }

    fun startListening() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        stopListening()

        showsListener = firestore.collection("users/$uid/shows").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val tmdbId = change.document.id.toIntOrNull() ?: return@forEach
                val remoteFavorite = change.document.getBoolean("isFavorite") ?: return@forEach
                val remoteModifiedAt = change.document.getLong("lastModifiedAt")?.let(Instant::ofEpochMilli) ?: return@forEach
                syncScope.launch {
                    val local = showDao.getShowOnce(tmdbId) ?: return@launch
                    if (remoteModifiedAt.isAfter(local.lastModifiedAt)) {
                        showDao.setShowFavorite(tmdbId, remoteFavorite, remoteModifiedAt)
                    }
                    _lastSyncedAt.value = Instant.now()
                }
            }
        }
        moviesListener = firestore.collection("users/$uid/movies").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val tmdbId = change.document.id.toIntOrNull() ?: return@forEach
                val remoteFavorite = change.document.getBoolean("isFavorite") ?: return@forEach
                val remoteModifiedAt = change.document.getLong("lastModifiedAt")?.let(Instant::ofEpochMilli) ?: return@forEach
                syncScope.launch {
                    val local = movieDao.getMovieOnce(tmdbId) ?: return@launch
                    if (remoteModifiedAt.isAfter(local.lastModifiedAt)) {
                        movieDao.setMovieFavorite(tmdbId, remoteFavorite, remoteModifiedAt)
                    }
                    _lastSyncedAt.value = Instant.now()
                }
            }
        }
    }

    fun stopListening() {
        showsListener?.remove()
        moviesListener?.remove()
        showsListener = null
        moviesListener = null
    }
}
