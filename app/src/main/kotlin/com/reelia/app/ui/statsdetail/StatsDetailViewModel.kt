package com.reelia.app.ui.statsdetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.parseShowBroadcastStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Backs the full-screen stats drill-down (genre / network / broadcast-status breakdown rows are
 * tappable on the Profile screen) — a dedicated scrollable screen rather than a ModalBottomSheet,
 * since a sheet capped at screen height silently cut off the tail of longer lists.
 */
@HiltViewModel
class StatsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    showRepository: ShowRepository,
    movieRepository: MovieRepository,
    imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    val title: String = Uri.decode(savedStateHandle.get<String>("filterLabel").orEmpty())

    private val filterType = StatsDetailFilterType.valueOf(savedStateHandle.get<String>("filterType")!!)
    private val filterId = Uri.decode(savedStateHandle.get<String>("filterId").orEmpty())

    val items: StateFlow<List<LibraryItemSummary>> = when (filterType) {
        StatsDetailFilterType.GENRE -> {
            val genreId = filterId.toIntOrNull()
            combine(
                showRepository.getAllShows(),
                showRepository.getShowGenreCrossRefs(),
                movieRepository.getAllMovies(),
                movieRepository.getMovieGenreCrossRefs(),
            ) { shows, showCrossRefs, movies, movieCrossRefs ->
                val showIds = showCrossRefs.filter { it.genreId == genreId }.map { it.showId }.toSet()
                val movieIds = movieCrossRefs.filter { it.genreId == genreId }.map { it.movieId }.toSet()
                val showItems = shows.filter { it.tmdbId in showIds }.map {
                    LibraryItemSummary(it.tmdbId, MediaType.TV, it.name, imageUrlBuilder.posterUrl(it.posterPath))
                }
                val movieItems = movies.filter { it.tmdbId in movieIds }.map {
                    LibraryItemSummary(it.tmdbId, MediaType.MOVIE, it.title, imageUrlBuilder.posterUrl(it.posterPath))
                }
                showItems + movieItems
            }
        }
        StatsDetailFilterType.NETWORK -> {
            showRepository.getAllShows().map { shows ->
                shows.filter { show ->
                    show.networkNames?.split(",").orEmpty().any { it.trim().equals(filterId, ignoreCase = true) }
                }.map { show ->
                    LibraryItemSummary(show.tmdbId, MediaType.TV, show.name, imageUrlBuilder.posterUrl(show.posterPath))
                }
            }
        }
        StatsDetailFilterType.BROADCAST_STATUS -> {
            showRepository.getAllShows().map { shows ->
                shows.filter { parseShowBroadcastStatus(it.broadcastStatus).name == filterId }
                    .map { show -> LibraryItemSummary(show.tmdbId, MediaType.TV, show.name, imageUrlBuilder.posterUrl(show.posterPath)) }
            }
        }
    }.map { items ->
        val collator = Collator.getInstance(Locale.getDefault())
        items.sortedWith { a, b -> collator.compare(a.title, b.title) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
