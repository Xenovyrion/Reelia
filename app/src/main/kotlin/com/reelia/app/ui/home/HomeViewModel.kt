package com.reelia.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.R
import com.reelia.app.data.auth.AuthRepository
import com.reelia.app.data.local.dao.ShowEpisodeProgress
import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.metadata.MetadataProviderRegistry
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.DiscoverCategory
import com.reelia.app.domain.model.MediaPreview
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.effectiveShowStatus
import com.reelia.app.ui.common.format.isAfterToday
import com.reelia.app.ui.common.format.toYearOrNull
import com.reelia.app.ui.common.model.buildUpcomingMovieItems
import com.reelia.app.ui.common.model.buildUpcomingShowItems
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class RawHomeData(
    val shows: List<TrackedShowEntity>,
    val progress: List<ShowEpisodeProgress>,
    val unwatchedEpisodes: List<EpisodeEntity>,
    val movies: List<TrackedMovieEntity>,
)

private data class DiscoverData(
    val movieCategory: DiscoverCategory = DiscoverCategory.POPULAR,
    val showCategory: DiscoverCategory = DiscoverCategory.POPULAR,
    val moviesByCategory: List<MediaPreview> = emptyList(),
    val showsByCategory: List<MediaPreview> = emptyList(),
    val isMoviesByCategoryLoading: Boolean = true,
    val isShowsByCategoryLoading: Boolean = true,
    val suggestions: List<MediaPreview> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val authRepository: AuthRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val discoverData = MutableStateFlow(DiscoverData())
    private val discoverLoading = MutableStateFlow(true)
    private val _pendingAddItems = MutableStateFlow<Set<Pair<MediaType, Int>>>(emptySet())
    private val _errorMessageRes = MutableStateFlow<Int?>(null)

    private val libraryItems = combine(
        showRepository.getAllShows(),
        movieRepository.getAllMovies(),
    ) { shows, movies ->
        shows.map { MediaType.TV to it.tmdbId }.toSet() + movies.map { MediaType.MOVIE to it.tmdbId }.toSet()
    }

    init {
        // One-time backfill: status used to only ever be set once at add-time, so anything
        // already fully watched before this fix shipped is stuck showing "à voir" until
        // reconciled against real progress here.
        viewModelScope.launch {
            showRepository.reconcileAllStatuses()
            movieRepository.reconcileAllStatuses()
        }

        // Movies, shows, and suggestions each update discoverData and flip their own loading
        // flag independently as soon as THEY resolve — previously all three were awaited
        // together before a single combined update, so a slow suggestions fetch (up to 3
        // sequential-ish TMDB recommendation calls) held back movies/shows that had already
        // arrived, reading as the whole section "popping in" late.
        viewModelScope.launch {
            val provider = metadataProviderRegistry.activeProvider.first()

            launch {
                val results = runCatching { provider.getMoviesByCategory(DiscoverCategory.POPULAR) }.getOrDefault(emptyList())
                discoverData.update { it.copy(moviesByCategory = results, isMoviesByCategoryLoading = false) }
                maybeStopDiscoverLoading()
            }

            launch {
                val results = runCatching { provider.getShowsByCategory(DiscoverCategory.POPULAR) }.getOrDefault(emptyList())
                discoverData.update { it.copy(showsByCategory = results, isShowsByCategoryLoading = false) }
                maybeStopDiscoverLoading()
            }

            launch {
                try {
                    val shows = showRepository.getAllShows().first()
                    val movies = movieRepository.getAllMovies().first()
                    val inLibrary = shows.map { MediaType.TV to it.tmdbId }.toSet() +
                        movies.map { MediaType.MOVIE to it.tmdbId }.toSet()

                    val suggestions = buildSuggestionSeeds(shows, movies)
                        .map { (type, id) ->
                            async { runCatching { provider.getRecommendationsFeed(type, id) }.getOrDefault(emptyList()) }
                        }
                        .awaitAll()
                        .flatten()
                        .distinctBy { it.mediaType to it.tmdbId }
                        .filterNot { (it.mediaType to it.tmdbId) in inLibrary }
                        .take(15)

                    discoverData.update { it.copy(suggestions = suggestions) }
                } catch (e: Exception) {
                    // Discovery rows are supplementary — a network failure here must never block
                    // the Room-backed core UI (continue watching), so they just stay empty.
                }
            }
        }
    }

    /** The full-screen "still loading" spinner only needs to wait for movies+shows (the two
     * sections with their own loading UI) — suggestions has no equivalent spinner and can
     * arrive whenever, so it's not part of this gate. */
    private fun maybeStopDiscoverLoading() {
        val discover = discoverData.value
        if (!discover.isMoviesByCategoryLoading && !discover.isShowsByCategoryLoading) {
            discoverLoading.value = false
        }
    }

    fun onMovieCategorySelected(category: DiscoverCategory) {
        if (category == discoverData.value.movieCategory) return
        viewModelScope.launch {
            discoverData.update { it.copy(movieCategory = category, isMoviesByCategoryLoading = true) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val results = provider.getMoviesByCategory(category)
                discoverData.update { it.copy(moviesByCategory = results, isMoviesByCategoryLoading = false) }
            } catch (e: Exception) {
                discoverData.update { it.copy(isMoviesByCategoryLoading = false) }
            }
        }
    }

    fun onShowCategorySelected(category: DiscoverCategory) {
        if (category == discoverData.value.showCategory) return
        viewModelScope.launch {
            discoverData.update { it.copy(showCategory = category, isShowsByCategoryLoading = true) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val results = provider.getShowsByCategory(category)
                discoverData.update { it.copy(showsByCategory = results, isShowsByCategoryLoading = false) }
            } catch (e: Exception) {
                discoverData.update { it.copy(isShowsByCategoryLoading = false) }
            }
        }
    }

    private val rawData = combine(
        showRepository.getAllShows(),
        showRepository.getEpisodeProgressByShow(),
        showRepository.getAllUnwatchedEpisodesOrdered(),
        movieRepository.getAllMovies(),
    ) { shows, progress, unwatchedEpisodes, movies ->
        RawHomeData(shows, progress, unwatchedEpisodes, movies)
    }

    private val userFirstName = authRepository.currentUser.map { user ->
        user?.displayName?.substringBefore(' ')?.takeIf { it.isNotBlank() }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        rawData,
        discoverData,
        userFirstName,
        discoverLoading,
        combine(libraryItems, _pendingAddItems, _errorMessageRes, ::Triple),
    ) { raw, discover, firstName, isDiscoverLoading, (libraryItems, pendingAddItems, errorMessageRes) ->
        val progressByShowId = raw.progress.associateBy { it.showId }
        // Excludes episodes that haven't aired yet from "next episode" consideration — a show
        // whose only remaining unwatched episodes are an announced-but-unreleased season has
        // nothing to actually continue with, so it shouldn't show up in Continue Watching.
        val nextEpisodeByShowId = raw.unwatchedEpisodes
            .filterNot { it.airDate.isAfterToday() }
            .groupBy { it.showId }
            .mapValues { it.value.first() }

        val continueWatching = raw.shows
            .filter { effectiveShowStatus(it.status, progressByShowId[it.tmdbId]) == WatchStatus.WATCHING }
            .mapNotNull { show ->
                val nextEpisode = nextEpisodeByShowId[show.tmdbId] ?: return@mapNotNull null
                val showProgress = progressByShowId[show.tmdbId]
                val progress = showProgress?.let { if (it.total == 0) 0f else it.watchedCount.toFloat() / it.total } ?: 0f
                ContinueWatchingItem(
                    showId = show.tmdbId,
                    title = show.name,
                    backdropUrl = imageUrlBuilder.backdropUrl(show.backdropPath),
                    seasonNumber = nextEpisode.seasonNumber,
                    episodeNumber = nextEpisode.episodeNumber,
                    episodeName = nextEpisode.name,
                    progress = progress,
                    status = effectiveShowStatus(show.status, showProgress),
                )
            }

        val greetingPeriod = when (LocalTime.now().hour) {
            in 5..11 -> GreetingPeriod.MORNING
            in 12..17 -> GreetingPeriod.AFTERNOON
            else -> GreetingPeriod.EVENING
        }

        HomeUiState(
            isLoading = false,
            isDiscoverLoading = isDiscoverLoading,
            greetingPeriod = greetingPeriod,
            userFirstName = firstName,
            continueWatching = continueWatching,
            upcomingShows = buildUpcomingShowItems(raw.shows, imageUrlBuilder),
            upcomingMovies = buildUpcomingMovieItems(raw.movies, imageUrlBuilder),
            movieCategory = discover.movieCategory,
            showCategory = discover.showCategory,
            moviesByCategory = discover.moviesByCategory.filterNot { (it.mediaType to it.tmdbId) in libraryItems }.map { it.toDiscoverItem() },
            showsByCategory = discover.showsByCategory.filterNot { (it.mediaType to it.tmdbId) in libraryItems }.map { it.toDiscoverItem() },
            isMoviesByCategoryLoading = discover.isMoviesByCategoryLoading,
            isShowsByCategoryLoading = discover.isShowsByCategoryLoading,
            suggestions = discover.suggestions.filterNot { (it.mediaType to it.tmdbId) in libraryItems }.map { it.toDiscoverItem() },
            pendingAddItems = pendingAddItems,
            errorMessageRes = errorMessageRes,
            favoriteShows = raw.shows.filter { it.isFavorite }.map { show ->
                HomeDiscoverItem(
                    tmdbId = show.tmdbId,
                    mediaType = MediaType.TV,
                    title = show.name,
                    posterUrl = imageUrlBuilder.posterUrl(show.posterPath),
                    year = show.firstAirDate.toYearOrNull(),
                )
            },
            favoriteMovies = raw.movies.filter { it.isFavorite }.map { movie ->
                HomeDiscoverItem(
                    tmdbId = movie.tmdbId,
                    mediaType = MediaType.MOVIE,
                    title = movie.title,
                    posterUrl = imageUrlBuilder.posterUrl(movie.posterPath),
                    year = movie.releaseDate.toYearOrNull(),
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onAddClicked(item: HomeDiscoverItem) {
        val key = item.mediaType to item.tmdbId
        if (key in _pendingAddItems.value) return
        viewModelScope.launch {
            _pendingAddItems.update { it + key }
            try {
                when (item.mediaType) {
                    MediaType.TV -> showRepository.addShowFromTmdb(item.tmdbId)
                    MediaType.MOVIE -> movieRepository.addMovieFromTmdb(item.tmdbId)
                }
            } catch (e: Exception) {
                _errorMessageRes.value = R.string.home_error_add
            } finally {
                _pendingAddItems.update { it - key }
            }
        }
    }

    fun onErrorShown() {
        _errorMessageRes.value = null
    }

    private suspend fun MediaPreview.toDiscoverItem(): HomeDiscoverItem = HomeDiscoverItem(
        tmdbId = tmdbId,
        mediaType = mediaType,
        title = title,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        year = releaseDate.toYearOrNull(),
    )

    /** Picks a handful of the user's titles to seed TMDB recommendations from: favorites
     * first (their strongest taste signal), falling back to most-recently-added when there
     * are no favorites yet. */
    private fun buildSuggestionSeeds(
        shows: List<TrackedShowEntity>,
        movies: List<TrackedMovieEntity>,
    ): List<Pair<MediaType, Int>> {
        val favorites = shows.filter { it.isFavorite }.map { MediaType.TV to it.tmdbId } +
            movies.filter { it.isFavorite }.map { MediaType.MOVIE to it.tmdbId }
        if (favorites.isNotEmpty()) return favorites.shuffled().take(3)

        val recentShows = shows.sortedByDescending { it.addedAt }.take(2).map { MediaType.TV to it.tmdbId }
        val recentMovies = movies.sortedByDescending { it.addedAt }.take(2).map { MediaType.MOVIE to it.tmdbId }
        return (recentShows + recentMovies).take(3)
    }
}
