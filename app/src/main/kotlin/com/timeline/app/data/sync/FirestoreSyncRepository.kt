package com.timeline.app.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.SyncOutboxDao
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
import dagger.Lazy
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
 * Cross-device sync over Firestore: pushes/pulls each show/movie's existence, `isFavorite`
 * flag, and watched-state, resolved last-write-wins per title via `lastModifiedAt`.
 *
 * Per-episode watched-state is folded into the *show's own* document (a `watchedEpisodes`
 * map) rather than a separate collection — conflict resolution is therefore per-show, not
 * per-episode: if both devices toggle different episodes of the same show while offline,
 * whichever device's change lands last in Firestore wins for the whole show. Fine for a
 * personal 2-device app where you're rarely watching the same show on both at once; not a
 * true CRDT-style merge.
 *
 * The watch log (for stats) is synced separately — see pushWatchLogEntry/listener wiring.
 */
@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val syncOutboxDao: SyncOutboxDao,
    private val showDao: ShowDao,
    private val movieDao: MovieDao,
    private val episodeDao: EpisodeDao,
    private val showRepository: Lazy<ShowRepository>,
    private val movieRepository: Lazy<MovieRepository>,
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
                MediaType.TV -> pushShow(uid, entry.tmdbId)
                MediaType.MOVIE -> pushMovie(uid, entry.tmdbId)
            }
            syncOutboxDao.clearPending(entry.tmdbId, entry.mediaType)
        }
        _lastSyncedAt.value = Instant.now()
    }

    private suspend fun pushShow(uid: String, tmdbId: Int) {
        val show = showDao.getShowOnce(tmdbId) ?: return
        val episodes = episodeDao.getEpisodesForShowOnce(tmdbId)
        val watchedEpisodes = episodes.associate { episode ->
            "${episode.seasonNumber}_${episode.episodeNumber}" to mapOf(
                "watched" to episode.watched,
                "watchedAt" to episode.watchedAt?.toEpochMilli(),
            )
        }
        firestore.collection("users/$uid/shows").document(tmdbId.toString())
            .set(
                mapOf(
                    "isFavorite" to show.isFavorite,
                    "lastModifiedAt" to show.lastModifiedAt.toEpochMilli(),
                    "watchedEpisodes" to watchedEpisodes,
                ),
            ).await()
    }

    private suspend fun pushMovie(uid: String, tmdbId: Int) {
        val movie = movieDao.getMovieOnce(tmdbId) ?: return
        firestore.collection("users/$uid/movies").document(tmdbId.toString())
            .set(
                mapOf(
                    "isFavorite" to movie.isFavorite,
                    "watched" to movie.watched,
                    "watchedAt" to movie.watchedAt?.toEpochMilli(),
                    "lastModifiedAt" to movie.lastModifiedAt.toEpochMilli(),
                ),
            ).await()
    }

    fun startListening() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        stopListening()

        showsListener = firestore.collection("users/$uid/shows").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val tmdbId = change.document.id.toIntOrNull() ?: return@forEach
                val remoteModifiedAt = change.document.getLong("lastModifiedAt")?.let(Instant::ofEpochMilli) ?: return@forEach
                val remoteFavorite = change.document.getBoolean("isFavorite") ?: false
                @Suppress("UNCHECKED_CAST")
                val watchedEpisodes = change.document.get("watchedEpisodes") as? Map<String, Map<String, Any?>> ?: emptyMap()
                syncScope.launch {
                    try {
                        val wasMissing = showDao.getShowOnce(tmdbId) == null
                        if (wasMissing) {
                            showRepository.get().fetchAndPersistFromTmdb(tmdbId)
                        }
                        val current = showDao.getShowOnce(tmdbId) ?: return@launch
                        if (wasMissing || remoteModifiedAt.isAfter(current.lastModifiedAt)) {
                            showDao.setShowFavorite(tmdbId, remoteFavorite, remoteModifiedAt)
                            watchedEpisodes.forEach { (key, value) ->
                                val parts = key.split("_")
                                val season = parts.getOrNull(0)?.toIntOrNull() ?: return@forEach
                                val episodeNumber = parts.getOrNull(1)?.toIntOrNull() ?: return@forEach
                                val watched = value["watched"] as? Boolean ?: false
                                val watchedAt = (value["watchedAt"] as? Long)?.let(Instant::ofEpochMilli)
                                episodeDao.setEpisodeWatched(tmdbId, season, episodeNumber, watched, watchedAt)
                            }
                        }
                        _lastSyncedAt.value = Instant.now()
                    } catch (e: Exception) {
                        // Live sync only — e.g. no TMDB API key set yet on this device, or no
                        // network. Never crash the listener; this entry retries next snapshot.
                    }
                }
            }
        }
        moviesListener = firestore.collection("users/$uid/movies").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val tmdbId = change.document.id.toIntOrNull() ?: return@forEach
                val remoteModifiedAt = change.document.getLong("lastModifiedAt")?.let(Instant::ofEpochMilli) ?: return@forEach
                val remoteFavorite = change.document.getBoolean("isFavorite") ?: false
                val remoteWatched = change.document.getBoolean("watched") ?: false
                val remoteWatchedAt = change.document.getLong("watchedAt")?.let(Instant::ofEpochMilli)
                syncScope.launch {
                    try {
                        val wasMissing = movieDao.getMovieOnce(tmdbId) == null
                        if (wasMissing) {
                            movieRepository.get().fetchAndPersistFromTmdb(tmdbId)
                        }
                        val current = movieDao.getMovieOnce(tmdbId) ?: return@launch
                        if (wasMissing || remoteModifiedAt.isAfter(current.lastModifiedAt)) {
                            movieDao.setMovieFavorite(tmdbId, remoteFavorite, remoteModifiedAt)
                            movieDao.setMovieWatched(tmdbId, remoteWatched, remoteWatchedAt, remoteModifiedAt)
                        }
                        _lastSyncedAt.value = Instant.now()
                    } catch (e: Exception) {
                        // Live sync only — see the shows listener's comment above.
                    }
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
