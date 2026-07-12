package com.timeline.app.ui.showdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.ShowBroadcastStatus
import com.timeline.app.domain.model.WatchProviderOption
import com.timeline.app.domain.model.parseShowBroadcastStatus
import com.timeline.app.domain.usecase.MarkEpisodeWatchedUseCase
import com.timeline.app.domain.usecase.MarkSeasonWatchedUseCase
import com.timeline.app.ui.common.components.CastRowItem
import com.timeline.app.ui.common.components.WatchProviderRowItem
import com.timeline.app.ui.common.format.toYearOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class ShowDetailExtras(
    val watchProvidersFlatrate: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersRent: List<WatchProviderRowItem> = emptyList(),
    val watchProvidersBuy: List<WatchProviderRowItem> = emptyList(),
    val trailerYoutubeKey: String? = null,
    val cast: List<CastRowItem> = emptyList(),
)

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val showRepository: ShowRepository,
    private val markEpisodeWatchedUseCase: MarkEpisodeWatchedUseCase,
    private val markSeasonWatchedUseCase: MarkSeasonWatchedUseCase,
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val showId: Int = checkNotNull(savedStateHandle["showId"])

    private val extras = MutableStateFlow(ShowDetailExtras())

    init {
        viewModelScope.launch {
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val preview = provider.getShowPreview(showId)
                extras.value = ShowDetailExtras(
                    watchProvidersFlatrate = preview.watchProviders?.flatrate.orEmpty().toRowItems(),
                    watchProvidersRent = preview.watchProviders?.rent.orEmpty().toRowItems(),
                    watchProvidersBuy = preview.watchProviders?.buy.orEmpty().toRowItems(),
                    trailerYoutubeKey = preview.trailerYoutubeKey,
                    cast = preview.cast.map {
                        CastRowItem(
                            personId = it.id,
                            name = it.name,
                            character = it.character,
                            photoUrl = imageUrlBuilder.posterUrl(it.profilePath, size = "w185"),
                        )
                    },
                )
            } catch (e: Exception) {
                // Live enrichment only — a network failure here must never block the
                // Room-backed core UI, so the extras just stay at their empty defaults.
            }
        }
    }

    val uiState: StateFlow<ShowDetailUiState> = combine(
        showRepository.getShowWithDetails(showId).filterNotNull(),
        extras,
        showRepository.getGenresForShow(showId),
    ) { details, extra, genres ->
        val episodesBySeason = details.episodes.groupBy { it.seasonNumber }
        val seasons = details.seasons
            .sortedBy { it.seasonNumber }
            .map { season ->
                SeasonUi(
                    seasonNumber = season.seasonNumber,
                    name = season.name,
                    episodeCount = season.episodeCount,
                    episodes = episodesBySeason[season.seasonNumber]
                        .orEmpty()
                        .sortedBy { it.episodeNumber }
                        .map {
                            EpisodeUi(
                                it.episodeNumber,
                                it.name,
                                it.watched,
                                it.overview,
                                it.voteAverage,
                                imageUrlBuilder.stillUrl(it.stillPath),
                            )
                        },
                )
            }
        val nextUnwatchedEpisode = seasons
            .flatMap { season -> season.episodes.map { season.seasonNumber to it } }
            .firstOrNull { (_, episode) -> !episode.watched }
            ?.let { (seasonNumber, episode) ->
                NextEpisodeUi(seasonNumber, episode.episodeNumber, episode.name, episode.stillUrl)
            }
        val broadcastStatus = parseShowBroadcastStatus(details.show.broadcastStatus)
        val firstYear = details.show.firstAirDate.toYearOrNull()
        val lastYear = details.show.lastAirDate.toYearOrNull()
        val yearRange = when {
            firstYear == null -> null
            broadcastStatus == ShowBroadcastStatus.RETURNING || broadcastStatus == ShowBroadcastStatus.IN_PRODUCTION ->
                "$firstYear –"
            lastYear == null || lastYear == firstYear -> firstYear
            else -> "$firstYear – $lastYear"
        }
        ShowDetailUiState(
            isLoading = false,
            title = details.show.name,
            overview = details.show.overview,
            posterUrl = imageUrlBuilder.posterUrl(details.show.posterPath),
            backdropUrl = imageUrlBuilder.backdropUrl(details.show.backdropPath),
            status = details.show.status,
            userRating = details.show.userRating,
            seasonCount = seasons.size,
            watchedEpisodeCount = seasons.sumOf { season -> season.episodes.count { it.watched } },
            totalEpisodeCount = seasons.sumOf { it.episodeCount },
            seasons = seasons,
            nextUnwatchedEpisode = nextUnwatchedEpisode,
            watchProvidersFlatrate = extra.watchProvidersFlatrate,
            watchProvidersRent = extra.watchProvidersRent,
            watchProvidersBuy = extra.watchProvidersBuy,
            trailerYoutubeKey = extra.trailerYoutubeKey,
            cast = extra.cast,
            broadcastStatus = broadcastStatus,
            networkNames = details.show.networkNames,
            yearRange = yearRange,
            genreNames = genres.map { it.name },
            nextEpisodeAirDate = details.show.nextEpisodeToAirDate,
            averageEpisodeRuntimeMinutes = details.show.averageEpisodeRuntimeMinutes,
            creatorNames = details.show.creatorNames,
            isFavorite = details.show.isFavorite,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShowDetailUiState(),
    )

    private suspend fun List<WatchProviderOption>.toRowItems(): List<WatchProviderRowItem> =
        map { WatchProviderRowItem(name = it.providerName, logoUrl = imageUrlBuilder.posterUrl(it.logoPath, size = "w92")) }

    fun onEpisodeToggled(seasonNumber: Int, episodeNumber: Int, watched: Boolean) {
        viewModelScope.launch {
            markEpisodeWatchedUseCase(showId, seasonNumber, episodeNumber, watched)
        }
    }

    fun onSeasonMarkAllWatched(seasonNumber: Int) {
        viewModelScope.launch {
            markSeasonWatchedUseCase(showId, seasonNumber)
        }
    }

    fun onSeasonExpanded(seasonNumber: Int) {
        viewModelScope.launch {
            val season = uiState.value.seasons.find { it.seasonNumber == seasonNumber }
            if (season != null && season.episodes.size < season.episodeCount) {
                showRepository.ensureSeasonEpisodesLoaded(showId, seasonNumber, defaultRuntimeMinutes = null)
            }
        }
    }

    fun onFavoriteToggled(isFavorite: Boolean) {
        viewModelScope.launch {
            showRepository.setFavorite(showId, isFavorite)
        }
    }
}
