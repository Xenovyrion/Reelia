package com.timeline.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.timeline.app.data.auth.AuthRepository
import com.timeline.app.data.local.prefs.LanguagePreferenceStore
import com.timeline.app.data.metadata.MetadataProvider
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.SettingsRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.data.repository.StatsRepository
import com.timeline.app.data.sync.FirestoreSyncRepository
import com.timeline.app.data.update.AppUpdateRepository
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.ui.common.components.BarChartEntry
import com.timeline.app.ui.common.components.GenreProgressItem
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.update.UpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    val genreBreakdown: List<GenreProgressItem> = emptyList(),
    val completedCount: Int = 0,
    val completedFraction: Float = 0f,
)

private data class StatsQuery(val scope: StatsScope, val weeklyOffset: Int, val monthlyOffset: Int)

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

    private fun completionFlow(scope: StatsScope): Flow<CompletionData> =
        combine(showRepository.getAllShows(), movieRepository.getAllMovies()) { shows, movies ->
            val statuses = when (scope) {
                StatsScope.ALL -> shows.map { it.status } + movies.map { it.status }
                StatsScope.SERIES -> shows.map { it.status }
                StatsScope.FILMS -> movies.map { it.status }
            }
            CompletionData(totalCount = statuses.size, completedCount = statuses.count { it == WatchStatus.COMPLETED })
        }

    private val statsQuery: Flow<StatsQuery> = combine(
        scopeState,
        weeklyOffsetState,
        monthlyOffsetState,
    ) { scope, weeklyOffset, monthlyOffset -> StatsQuery(scope, weeklyOffset, monthlyOffset) }

    val statsUiState: StateFlow<ProfileStatsUiState> = statsQuery.flatMapLatest { query ->
        val mediaType = query.scope.toMediaType()
        combine(
            statsRepository.getBasicStats(mediaType),
            statsRepository.getWeeklyBreakdown(mediaType, periodsAgo = query.weeklyOffset),
            statsRepository.getMonthlyBreakdown(mediaType, periodsAgo = query.monthlyOffset),
            statsRepository.getGenreBreakdown(mediaType, limit = 5),
            completionFlow(query.scope),
        ) { basic, weekly, monthly, genres, completion ->
            val totalGenreMinutes = genres.sumOf { it.totalMinutes }
            ProfileStatsUiState(
                scope = query.scope,
                totalHoursWatched = basic.totalMinutesWatched / 60.0,
                totalWatchedCount = basic.totalWatchedCount,
                weeklyChart = weekly.map { BarChartEntry(it.label, it.minutesWatched / 60f) },
                weeklyOffset = query.weeklyOffset,
                monthlyChart = monthly.map { BarChartEntry(it.label, it.minutesWatched / 60f) },
                monthlyOffset = query.monthlyOffset,
                genreBreakdown = genres.mapIndexed { index, genre ->
                    GenreProgressItem(
                        genreId = genre.genreId,
                        name = genre.genreName,
                        fraction = if (totalGenreMinutes == 0) 0f else genre.totalMinutes.toFloat() / totalGenreMinutes,
                        color = GenrePalette[index % GenrePalette.size],
                    )
                },
                completedCount = completion.completedCount,
                completedFraction = if (completion.totalCount == 0) 0f else completion.completedCount.toFloat() / completion.totalCount,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileStatsUiState(),
    )

    fun onStatsScopeSelected(scope: StatsScope) {
        scopeState.value = scope
    }

    // Chart paging — shifts the visible 12-period window one step at a time rather than by a
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
