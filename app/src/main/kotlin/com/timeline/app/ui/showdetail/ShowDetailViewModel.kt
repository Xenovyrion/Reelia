package com.timeline.app.ui.showdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.usecase.MarkEpisodeWatchedUseCase
import com.timeline.app.domain.usecase.MarkSeasonWatchedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val showRepository: ShowRepository,
    private val markEpisodeWatchedUseCase: MarkEpisodeWatchedUseCase,
    private val markSeasonWatchedUseCase: MarkSeasonWatchedUseCase,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val showId: Int = checkNotNull(savedStateHandle["showId"])

    val uiState: StateFlow<ShowDetailUiState> = showRepository.getShowWithDetails(showId)
        .filterNotNull()
        .map { details ->
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
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShowDetailUiState(),
        )

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
}
