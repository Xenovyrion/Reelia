package com.reelia.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.auth.AuthRepository
import com.reelia.app.data.local.dao.ShowEpisodeProgress
import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.metadata.MetadataProviderRegistry
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaPreview
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.ui.common.effectiveShowStatus
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
import kotlinx.coroutines.launch

private data class RawHomeData(
    val shows: List<TrackedShowEntity>,
    val progress: List<ShowEpisodeProgress>,
    val unwatchedEpisodes: List<EpisodeEntity>,
    val movies: List<TrackedMovieEntity>,
)

private data class DiscoverData(
    val trending: List<MediaPreview> = emptyList(),
    val recentMovies: List<MediaPreview> = emptyList(),
    val recentShows: List<MediaPreview> = emptyList(),
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

    init {
        // One-time backfill: status used to only ever be set once at add-time, so anything
        // already fully watched before this fix shipped is stuck showing "à voir" until
        // reconciled against real progress here.
        viewModelScope.launch {
            showRepository.reconcileAllStatuses()
            movieRepository.reconcileAllStatuses()
        }

        viewModelScope.launch {
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                // Each wrapped independently (not just by the outer try/catch) so one failing
                // feed — e.g. no TMDB API key configured yet on a fresh install — can't take
                // down the other two, or the whole coroutine, along with it.
                val trendingDeferred = async { runCatching { provider.getTrendingFeed() }.getOrDefault(emptyList()) }
                val recentMoviesDeferred = async { runCatching { provider.getRecentMoviesFeed() }.getOrDefault(emptyList()) }
                val recentShowsDeferred = async { runCatching { provider.getRecentShowsFeed() }.getOrDefault(emptyList()) }

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

                discoverData.value = DiscoverData(
                    trending = trendingDeferred.await(),
                    recentMovies = recentMoviesDeferred.await(),
                    recentShows = recentShowsDeferred.await(),
                    suggestions = suggestions,
                )
            } catch (e: Exception) {
                // Discovery rows are supplementary — a network failure here must never block
                // the Room-backed core UI (continue watching), so they just stay empty.
            } finally {
                discoverLoading.value = false
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
    ) { raw, discover, firstName, isDiscoverLoading ->
        val progressByShowId = raw.progress.associateBy { it.showId }
        val nextEpisodeByShowId = raw.unwatchedEpisodes.groupBy { it.showId }.mapValues { it.value.first() }

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
            trending = discover.trending.map { it.toDiscoverItem() },
            recentMovies = discover.recentMovies.map { it.toDiscoverItem() },
            recentShows = discover.recentShows.map { it.toDiscoverItem() },
            suggestions = discover.suggestions.map { it.toDiscoverItem() },
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
