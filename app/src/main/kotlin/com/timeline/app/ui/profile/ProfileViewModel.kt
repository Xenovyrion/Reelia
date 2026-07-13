package com.timeline.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.timeline.app.data.auth.AuthRepository
import com.timeline.app.data.local.dao.GenreStat
import com.timeline.app.data.local.entity.EpisodeEntity
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.data.metadata.MetadataProvider
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.BasicStats
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.SettingsRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.data.repository.StatsRepository
import com.timeline.app.data.repository.TimeBucketEntry
import com.timeline.app.data.sync.FirestoreSyncRepository
import com.timeline.app.data.update.AppUpdateRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.ShowBroadcastStatus
import com.timeline.app.domain.model.parseShowBroadcastStatus
import com.timeline.app.ui.common.components.BarChartEntry
import com.timeline.app.ui.common.components.GenreProgressItem
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.update.UpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StatsScope { ALL, SERIES, FILMS }

private fun StatsScope.toMediaType(): MediaType? = when (this) {
    StatsScope.ALL -> null
    StatsScope.SERIES -> MediaType.TV
    StatsScope.FILMS -> MediaType.MOVIE
}

private data class CompletionData(val totalCount: Int, val completedCount: Int)

/** Turns a raw "YYYY-MM" bucket label into a locale-aware short month name + 2-digit year,
 * e.g. "Janv. 27" (FR) / "Jan 27" (EN) — matches this app's per-user language preference rather
 * than the device locale. */
private fun formatMonthLabel(rawLabel: String): String {
    val yearMonth = YearMonth.parse(rawLabel)
    val locale = Locale.getDefault()
    val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, locale)
        .replaceFirstChar { it.titlecase(locale) }
    val shortYear = (yearMonth.year % 100).toString().padStart(2, '0')
    return "$monthName $shortYear"
}

/** Turns a raw ISO day-of-week number ("1".."7") into a locale-aware short weekday name, e.g.
 * "Lun." (FR) / "Mon" (EN). */
private fun formatWeekdayLabel(rawLabel: String): String {
    val locale = Locale.getDefault()
    return DayOfWeek.of(rawLabel.toInt()).getDisplayName(TextStyle.SHORT, locale)
        .replaceFirstChar { it.titlecase(locale) }
}

private val GenrePalette = listOf(StatusWatchingCompleted, StatusWantToWatch, StatusPlanned, StatusFavorite)

data class ProfileUiState(
    val apiKey: String? = null,
    val language: String = LanguagePreferenceStore.FALLBACK_LANGUAGE,
    val selectedProviderId: String = "tmdb",
    val accountEmail: String? = null,
    val lastSyncedAt: Instant? = null,
)

data class ProfileStatsUiState(
    val scope: StatsScope = StatsScope.ALL,
    val totalHoursWatched: Double = 0.0,
    val totalWatchedCount: Int = 0,
    val weeklyChart: List<BarChartEntry> = emptyList(),
    val weeklyOffset: Int = 0,
    val monthlyChart: List<BarChartEntry> = emptyList(),
    val monthlyOffset: Int = 0,
    val weekdayChart: List<BarChartEntry> = emptyList(),
    val genreBreakdown: List<GenreProgressItem> = emptyList(),
    val completedCount: Int = 0,
    val completedFraction: Float = 0f,
    val remainingCount: Int = 0,
    val remainingHoursEstimate: Double = 0.0,
    val averageHoursPerWeek: Double = 0.0,
    val showsAddedCount: Int = 0,
    val showsAiringCount: Int = 0,
    val showsByBroadcastStatus: List<BroadcastStatusStat> = emptyList(),
    val networkBreakdown: List<NetworkStat> = emptyList(),
)

data class BroadcastStatusStat(val status: ShowBroadcastStatus, val count: Int, val fraction: Float)

data class NetworkStat(val name: String, val count: Int, val fraction: Float)

private data class StatsQuery(val scope: StatsScope, val weeklyOffset: Int, val monthlyOffset: Int)

private data class CoreStats(
    val basic: BasicStats,
    val weekly: List<TimeBucketEntry>,
    val monthly: List<TimeBucketEntry>,
    val genres: List<GenreStat>,
    val completion: CompletionData,
)

private data class ExtraStats(
    val shows: List<TrackedShowEntity>,
    val unwatchedEpisodes: List<EpisodeEntity>,
    val movies: List<TrackedMovieEntity>,
    val weekday: List<TimeBucketEntry>,
)

data class GenreLibraryItem(val id: Int, val mediaType: MediaType, val title: String, val posterUrl: String?)

data class DeleteAccountUiState(
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val requiresRecentLogin: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val appUpdateRepository: AppUpdateRepository,
    private val statsRepository: StatsRepository,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
    metadataProviderRegistry: MetadataProviderRegistry,
) : ViewModel() {

    val providers: List<MetadataProvider> = metadataProviderRegistry.providers

    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.apiKey,
        settingsRepository.language,
        settingsRepository.selectedProviderId,
        authRepository.currentUser,
        firestoreSyncRepository.lastSyncedAt,
    ) { apiKey, language, providerId, user, lastSyncedAt ->
        ProfileUiState(
            apiKey = apiKey,
            language = language,
            selectedProviderId = providerId,
            accountEmail = user?.email,
            lastSyncedAt = lastSyncedAt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    private val scopeState = MutableStateFlow(StatsScope.ALL)
    private val weeklyOffsetState = MutableStateFlow(0)
    private val monthlyOffsetState = MutableStateFlow(0)

    // "Completed" is derived from actual watch progress rather than the user-set WatchStatus
    // field — WatchStatus.COMPLETED is never set automatically anywhere in this app (it's only
    // ever assigned PLAN_TO_WATCH at add-time), so counting it directly always reads as 0. A show
    // is complete when every known episode is watched; a movie is complete when its `watched`
    // flag is set.
    private fun completionFlow(scope: StatsScope): Flow<CompletionData> =
        combine(
            showRepository.getAllShows(),
            showRepository.getEpisodeProgressByShow(),
            movieRepository.getAllMovies(),
        ) { shows, showProgress, movies ->
            val progressByShowId = showProgress.associateBy { it.showId }
            val completedShowCount = shows.count { show ->
                val progress = progressByShowId[show.tmdbId]
                progress != null && progress.total > 0 && progress.watchedCount == progress.total
            }
            val completedMovieCount = movies.count { it.watched }
            when (scope) {
                StatsScope.ALL -> CompletionData(
                    totalCount = shows.size + movies.size,
                    completedCount = completedShowCount + completedMovieCount,
                )
                StatsScope.SERIES -> CompletionData(totalCount = shows.size, completedCount = completedShowCount)
                StatsScope.FILMS -> CompletionData(totalCount = movies.size, completedCount = completedMovieCount)
            }
        }

    private val statsQuery: Flow<StatsQuery> = combine(
        scopeState,
        weeklyOffsetState,
        monthlyOffsetState,
    ) { scope, weeklyOffset, monthlyOffset -> StatsQuery(scope, weeklyOffset, monthlyOffset) }

    val statsUiState: StateFlow<ProfileStatsUiState> = statsQuery.flatMapLatest { query ->
        val mediaType = query.scope.toMediaType()
        val coreFlow = combine(
            statsRepository.getBasicStats(mediaType),
            statsRepository.getWeeklyBreakdown(mediaType, periodsAgo = query.weeklyOffset),
            statsRepository.getMonthlyBreakdown(mediaType, periodsAgo = query.monthlyOffset),
            statsRepository.getGenreBreakdown(mediaType, limit = 5),
            completionFlow(query.scope),
        ) { basic, weekly, monthly, genres, completion -> CoreStats(basic, weekly, monthly, genres, completion) }

        val extraFlow = combine(
            showRepository.getAllShows(),
            showRepository.getAllUnwatchedEpisodesOrdered(),
            movieRepository.getAllMovies(),
            statsRepository.getWeekdayBreakdown(mediaType),
        ) { shows, unwatchedEpisodes, movies, weekday -> ExtraStats(shows, unwatchedEpisodes, movies, weekday) }

        combine(coreFlow, extraFlow) { core, extra -> buildStatsUiState(query, core, extra) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileStatsUiState(),
    )

    private fun buildStatsUiState(query: StatsQuery, core: CoreStats, extra: ExtraStats): ProfileStatsUiState {
        val totalGenreMinutes = core.genres.sumOf { it.totalMinutes }

        // "Remaining" mixes unwatched episodes (Séries/Tout) and un-watched movies
        // (Films/Tout) depending on scope, so it always reflects what's left in the library.
        val unwatchedEpisodes = if (query.scope == StatsScope.FILMS) emptyList() else extra.unwatchedEpisodes
        val remainingMovies = if (query.scope == StatsScope.SERIES) emptyList() else extra.movies.filter { !it.watched }
        val showById = extra.shows.associateBy { it.tmdbId }
        val remainingMinutes = unwatchedEpisodes.sumOf { episode ->
            episode.runtimeMinutes ?: showById[episode.showId]?.averageEpisodeRuntimeMinutes ?: 0
        } + remainingMovies.sumOf { it.runtimeMinutes ?: 0 }

        val averageHoursPerWeek = if (core.weekly.isEmpty()) {
            0.0
        } else {
            core.weekly.sumOf { it.minutesWatched }.toDouble() / core.weekly.size / 60.0
        }

        // Shows-added / broadcast-status / network breakdowns are inherently series-only —
        // they collapse to empty/zero when browsing the Films scope.
        val showsForScope = if (query.scope == StatsScope.FILMS) emptyList() else extra.shows
        val broadcastCounts = showsForScope.groupingBy { parseShowBroadcastStatus(it.broadcastStatus) }.eachCount()
        val showsByBroadcastStatus = if (showsForScope.isEmpty()) {
            emptyList()
        } else {
            broadcastCounts.entries.sortedByDescending { it.value }.map { (status, count) ->
                BroadcastStatusStat(status = status, count = count, fraction = count.toFloat() / showsForScope.size)
            }
        }
        val networkCounts = showsForScope
            .flatMap { it.networkNames?.split(",").orEmpty().map(String::trim) }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
        val totalNetworkMentions = networkCounts.values.sum()
        val networkBreakdown = if (totalNetworkMentions == 0) {
            emptyList()
        } else {
            networkCounts.entries.sortedByDescending { it.value }.take(5).map { (name, count) ->
                NetworkStat(name = name, count = count, fraction = count.toFloat() / totalNetworkMentions)
            }
        }

        return ProfileStatsUiState(
            scope = query.scope,
            totalHoursWatched = core.basic.totalMinutesWatched / 60.0,
            totalWatchedCount = core.basic.totalWatchedCount,
            weeklyChart = core.weekly.map { BarChartEntry(it.label, it.minutesWatched / 60f) },
            weeklyOffset = query.weeklyOffset,
            monthlyChart = core.monthly.map { BarChartEntry(formatMonthLabel(it.label), it.minutesWatched / 60f) },
            monthlyOffset = query.monthlyOffset,
            weekdayChart = extra.weekday.map { BarChartEntry(formatWeekdayLabel(it.label), it.minutesWatched / 60f) },
            genreBreakdown = core.genres.mapIndexed { index, genre ->
                GenreProgressItem(
                    genreId = genre.genreId,
                    name = genre.genreName,
                    fraction = if (totalGenreMinutes == 0) 0f else genre.totalMinutes.toFloat() / totalGenreMinutes,
                    color = GenrePalette[index % GenrePalette.size],
                )
            },
            completedCount = core.completion.completedCount,
            completedFraction = if (core.completion.totalCount == 0) 0f else core.completion.completedCount.toFloat() / core.completion.totalCount,
            remainingCount = unwatchedEpisodes.size + remainingMovies.size,
            remainingHoursEstimate = remainingMinutes / 60.0,
            averageHoursPerWeek = averageHoursPerWeek,
            showsAddedCount = showsForScope.size,
            // TMDB's "In Production" status only covers pre-air shows (announced/greenlit but no
            // episodes aired yet) — a show like House of the Dragon that's airing and renewed for
            // more seasons is reported as "Returning Series", which is what this counts.
            showsAiringCount = broadcastCounts[ShowBroadcastStatus.RETURNING] ?: 0,
            showsByBroadcastStatus = showsByBroadcastStatus,
            networkBreakdown = networkBreakdown,
        )
    }

    fun onStatsScopeSelected(scope: StatsScope) {
        scopeState.value = scope
    }

    // Chart paging — shifts the visible period window one step at a time rather than by a
    // full page, so it reads as scrolling smoothly back through time. "Next" is clamped at 0
    // (the present) since there's nothing to show beyond it.
    fun onWeeklyChartPrevious() {
        weeklyOffsetState.update { it + 1 }
    }

    fun onWeeklyChartNext() {
        weeklyOffsetState.update { (it - 1).coerceAtLeast(0) }
    }

    fun onMonthlyChartPrevious() {
        monthlyOffsetState.update { it + 1 }
    }

    fun onMonthlyChartNext() {
        monthlyOffsetState.update { (it - 1).coerceAtLeast(0) }
    }

    // Genre drill-down bottom sheet: shows/movies matching whichever genre was tapped in the
    // breakdown list, shown as a ModalBottomSheet overlay rather than a full navigation screen.
    private val selectedGenreIdState = MutableStateFlow<Int?>(null)

    val genreLibraryItems: StateFlow<List<GenreLibraryItem>> = selectedGenreIdState.flatMapLatest { genreId ->
        if (genreId == null) {
            flowOf(emptyList())
        } else {
            combine(
                showRepository.getAllShows(),
                showRepository.getShowGenreCrossRefs(),
                movieRepository.getAllMovies(),
                movieRepository.getMovieGenreCrossRefs(),
            ) { shows, showCrossRefs, movies, movieCrossRefs ->
                val showIds = showCrossRefs.filter { it.genreId == genreId }.map { it.showId }.toSet()
                val movieIds = movieCrossRefs.filter { it.genreId == genreId }.map { it.movieId }.toSet()
                val showItems = shows.filter { it.tmdbId in showIds }.map {
                    GenreLibraryItem(it.tmdbId, MediaType.TV, it.name, imageUrlBuilder.posterUrl(it.posterPath))
                }
                val movieItems = movies.filter { it.tmdbId in movieIds }.map {
                    GenreLibraryItem(it.tmdbId, MediaType.MOVIE, it.title, imageUrlBuilder.posterUrl(it.posterPath))
                }
                showItems + movieItems
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun onGenreSelected(genreId: Int?) {
        selectedGenreIdState.value = genreId
    }

    // Network drill-down bottom sheet — same idea as the genre one above, but shows only (movies
    // have no network field) filtered by a case-insensitive match against the comma-separated
    // networkNames string, since there's no cross-ref table for networks.
    private val selectedNetworkState = MutableStateFlow<String?>(null)

    val networkLibraryItems: StateFlow<List<GenreLibraryItem>> = selectedNetworkState.flatMapLatest { network ->
        if (network == null) {
            flowOf(emptyList())
        } else {
            showRepository.getAllShows().map { shows ->
                shows.filter { show ->
                    show.networkNames?.split(",").orEmpty().any { it.trim().equals(network, ignoreCase = true) }
                }.map { show ->
                    GenreLibraryItem(show.tmdbId, MediaType.TV, show.name, imageUrlBuilder.posterUrl(show.posterPath))
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun onNetworkSelected(network: String?) {
        selectedNetworkState.value = network
    }

    fun onSignOut() {
        authRepository.signOut()
    }

    private val saveEventChannel = Channel<Unit>(Channel.BUFFERED)
    val saveEvent: Flow<Unit> = saveEventChannel.receiveAsFlow()

    fun onApiKeySubmitted(key: String) {
        viewModelScope.launch {
            val trimmedKey = key.trim()
            settingsRepository.setApiKey(trimmedKey)
            saveEventChannel.send(Unit)
            // Push so the other device can import this key automatically instead of requiring
            // it to be re-typed after every reinstall.
            firestoreSyncRepository.pushApiKey(trimmedKey)
            // Retry sync hydration: on a fresh install, remote shows/movies can only be fetched
            // from TMDB once a key is set here, so any hydration that failed for lack of a key
            // needs a fresh listener snapshot to retry now that one exists.
            firestoreSyncRepository.startListening()
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(languageCode)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(LanguagePreferenceStore.uiLocaleTagFor(languageCode)),
            )
            firestoreSyncRepository.pushLanguage(languageCode)
        }
    }

    fun onProviderSelected(providerId: String) {
        viewModelScope.launch { settingsRepository.setSelectedProviderId(providerId) }
    }

    // Manual "check for updates" flow — an Activity-scoped UpdateBanner (see ui.update) also
    // auto-checks once per process independently; this is the Profile screen's own entry point.
    private val _updateUiState = MutableStateFlow(UpdateUiState())
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    fun onCheckForUpdateClicked() {
        viewModelScope.launch {
            _updateUiState.update { it.copy(isChecking = true, errorMessage = null) }
            val update = appUpdateRepository.checkForUpdate()
            _updateUiState.update { it.copy(isChecking = false, hasChecked = true, availableUpdate = update) }
        }
    }

    fun onUpdateDownloadClicked() {
        val update = _updateUiState.value.availableUpdate ?: return
        viewModelScope.launch {
            _updateUiState.update { it.copy(isDownloading = true, errorMessage = null) }
            try {
                val uri = appUpdateRepository.downloadUpdate(update)
                _updateUiState.update { it.copy(isDownloading = false, downloadedApkUri = uri) }
            } catch (e: Exception) {
                _updateUiState.update { it.copy(isDownloading = false, errorMessage = e.message) }
            }
        }
    }

    fun buildInstallIntent(apkUri: Uri): Intent = appUpdateRepository.buildInstallIntent(apkUri)

    fun onInstallLaunched() {
        _updateUiState.update { it.copy(downloadedApkUri = null) }
    }

    // Account deletion
    private val _deleteAccountUiState = MutableStateFlow(DeleteAccountUiState())
    val deleteAccountUiState: StateFlow<DeleteAccountUiState> = _deleteAccountUiState.asStateFlow()

    fun onDeleteAccountConfirmed() {
        viewModelScope.launch {
            _deleteAccountUiState.update { it.copy(isDeleting = true, errorMessage = null, requiresRecentLogin = false) }
            try {
                firestoreSyncRepository.deleteAccountAndAllData()
                // A successful delete invalidates the Firebase session — AuthGateViewModel's
                // listener reacts to that the same way it does for a normal sign-out, so no
                // local navigation call is needed here.
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                _deleteAccountUiState.update { it.copy(isDeleting = false, requiresRecentLogin = true) }
            } catch (e: Exception) {
                _deleteAccountUiState.update { it.copy(isDeleting = false, errorMessage = e.message) }
            }
        }
    }

    fun onDeleteAccountErrorDismissed() {
        _deleteAccountUiState.update { it.copy(errorMessage = null, requiresRecentLogin = false) }
    }
}
