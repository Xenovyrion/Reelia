package com.timeline.app.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.timeline.app.data.local.AppDatabase
import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.SyncOutboxDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.data.local.prefs.TmdbApiKeyStore
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
import kotlinx.coroutines.withContext

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
    private val watchLogDao: WatchLogDao,
    private val tmdbApiKeyStore: TmdbApiKeyStore,
    private val languagePreferenceStore: LanguagePreferenceStore,
    private val appDatabase: AppDatabase,
    private val showRepository: Lazy<ShowRepository>,
    private val movieRepository: Lazy<MovieRepository>,
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var showsListener: ListenerRegistration? = null
    private var moviesListener: ListenerRegistration? = null
    private var watchLogListener: ListenerRegistration? = null
    private var userDocListener: ListenerRegistration? = null

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

    /** Fire-and-forget variant of [pushPendingChanges] for bulk local writes (e.g. TV Time
     * import) where the outbox can hold hundreds of entries — blocking the caller on that many
     * sequential Firestore writes would hang whatever screen is waiting on it. Runs on this
     * repository's own long-lived scope so it keeps going after the caller moves on. */
    fun pushPendingChangesInBackground() {
        syncScope.launch { runCatching { pushPendingChanges() } }
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

    /** Pushes one watch-log entry to Firestore right away — the watch log is append-only, so
     * unlike shows/movies it doesn't go through the outbox, it's fire-and-forget per insert. */
    suspend fun pushWatchLogEntry(entry: WatchLogEntryEntity) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users/$uid/watchLog").document(entry.syncId)
            .set(
                mapOf(
                    "mediaType" to entry.mediaType.name,
                    "tmdbId" to entry.tmdbId,
                    "seasonNumber" to entry.seasonNumber,
                    "episodeNumber" to entry.episodeNumber,
                    "runtimeMinutes" to entry.runtimeMinutes,
                    "watchedAt" to entry.watchedAt.toEpochMilli(),
                ),
            ).await()
    }

    /** Pushes the TMDB API key to the user's own Firestore document (merged, not overwritten)
     * so the other device can import it automatically instead of requiring it to be re-typed
     * after every reinstall. Protected by the same per-user Firestore rule as everything else
     * — only this account's authenticated UID can read or write it. */
    suspend fun pushApiKey(key: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("tmdbApiKey" to key), SetOptions.merge())
            .await()
    }

    /** Pushes the language/content-locale preference the same way as the API key — merged into
     * the user's own document, imported by the other device via the same listener. */
    suspend fun pushLanguage(language: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("language" to language), SetOptions.merge())
            .await()
    }

    /**
     * Permanently deletes this account's Firestore data and the Firebase Auth user itself.
     *
     * Order matters: Firestore data must be wiped *before* the Auth user is deleted, since these
     * deletes are gated by a security rule requiring `request.auth.uid == userId` — once the user
     * is deleted that session is invalidated and nothing could authorize cleanup afterward (this
     * project has no server-side Cloud Functions to do it with admin rights; that requires the
     * paid Blaze plan, deliberately avoided for a personal Spark-plan app). If the final Auth
     * delete call fails because Firebase requires a recent sign-in, the Firestore data is already
     * gone but the Auth account survives — the caller should ask the user to sign out, sign back
     * in, and retry, which resets Firebase's "recent login" clock.
     */
    suspend fun deleteAccountAndAllData() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        stopListening()
        deleteAllDocuments("users/$uid/shows")
        deleteAllDocuments("users/$uid/movies")
        deleteAllDocuments("users/$uid/watchLog")
        firestore.collection("users").document(uid).delete().await()
        // clearAllTables() is a blocking call, not a suspend fun — this whole function runs on
        // viewModelScope's Main dispatcher by default, so without this it hits Room's main-thread
        // guard ("Cannot access database on the main thread").
        withContext(Dispatchers.IO) { appDatabase.clearAllTables() }
        firebaseAuth.currentUser?.delete()?.await()
    }

    /** Deletes every document in a collection using batched writes (up to 500 per Firestore
     * batch, chunked at 400 for headroom) instead of one sequential delete per document — a
     * library's watch log alone can be several thousand documents, and awaiting each delete
     * individually could take tens of minutes. */
    private suspend fun deleteAllDocuments(collectionPath: String) {
        val documents = firestore.collection(collectionPath).get().await().documents
        documents.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    /**
     * Wipes all library data — tracked shows/movies, episodes, watch log — locally and (if
     * signed in) in Firestore, without touching the account itself or app settings (TMDB key,
     * language). Used by the "reset library" action in Settings, e.g. to redo a botched import.
     */
    suspend fun resetLibrary() {
        val uid = firebaseAuth.currentUser?.uid
        // Stopped first, same as deleteAccountAndAllData — otherwise a live snapshot listener
        // can race the wipe and re-insert a show/movie right as its Firestore doc is deleted.
        stopListening()
        if (uid != null) {
            deleteAllDocuments("users/$uid/shows")
            deleteAllDocuments("users/$uid/movies")
            deleteAllDocuments("users/$uid/watchLog")
        }
        withContext(Dispatchers.IO) { appDatabase.clearAllTables() }
        if (uid != null) startListening()
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
        watchLogListener = firestore.collection("users/$uid/watchLog").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                if (change.type != DocumentChange.Type.ADDED) return@forEach
                val syncId = change.document.id
                val mediaType = change.document.getString("mediaType")?.let {
                    runCatching { MediaType.valueOf(it) }.getOrNull()
                } ?: return@forEach
                val tmdbId = change.document.getLong("tmdbId")?.toInt() ?: return@forEach
                val seasonNumber = change.document.getLong("seasonNumber")?.toInt()
                val episodeNumber = change.document.getLong("episodeNumber")?.toInt()
                val runtimeMinutes = change.document.getLong("runtimeMinutes")?.toInt() ?: 0
                val watchedAt = change.document.getLong("watchedAt")?.let(Instant::ofEpochMilli) ?: return@forEach
                syncScope.launch {
                    try {
                        if (watchLogDao.countBySyncId(syncId) == 0) {
                            watchLogDao.insert(
                                WatchLogEntryEntity(
                                    syncId = syncId,
                                    mediaType = mediaType,
                                    tmdbId = tmdbId,
                                    seasonNumber = seasonNumber,
                                    episodeNumber = episodeNumber,
                                    runtimeMinutes = runtimeMinutes,
                                    watchedAt = watchedAt,
                                ),
                            )
                        }
                        _lastSyncedAt.value = Instant.now()
                    } catch (e: Exception) {
                        // Live sync only — see the shows listener's comment above.
                    }
                }
            }
        }
        userDocListener = firestore.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener

            val remoteKey = snapshot.getString("tmdbApiKey")
            if (!remoteKey.isNullOrBlank() && remoteKey != tmdbApiKeyStore.currentKey) {
                syncScope.launch {
                    try {
                        // Import the key from the other device, then restart listening so any
                        // show/movie hydration that previously failed for lack of a key retries now.
                        tmdbApiKeyStore.setApiKey(remoteKey)
                        startListening()
                    } catch (e: Exception) {
                        // Live sync only — see the shows listener's comment above.
                    }
                }
            }

            val remoteLanguage = snapshot.getString("language")
            if (!remoteLanguage.isNullOrBlank() && remoteLanguage != languagePreferenceStore.currentLanguage) {
                syncScope.launch {
                    try {
                        languagePreferenceStore.setLanguage(remoteLanguage)
                        withContext(Dispatchers.Main) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(LanguagePreferenceStore.uiLocaleTagFor(remoteLanguage)),
                            )
                        }
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
        watchLogListener?.remove()
        userDocListener?.remove()
        showsListener = null
        moviesListener = null
        watchLogListener = null
        userDocListener = null
    }
}
