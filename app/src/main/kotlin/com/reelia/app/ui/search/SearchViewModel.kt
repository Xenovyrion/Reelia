package com.reelia.app.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.R
import com.reelia.app.data.metadata.MetadataProviderRegistry
import com.reelia.app.data.remote.tmdb.MissingTmdbApiKeyException
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.SettingsRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaPreview
import com.reelia.app.domain.model.MediaType
import com.reelia.app.domain.model.TmdbSearchResult
import com.reelia.app.ui.common.components.GenreOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Below this length, TMDB results are too broad to be useful and every keystroke would still
 * fire a network call — wait for at least this many characters before searching. */
private const val MIN_QUERY_LENGTH = 2
private const val SEARCH_DEBOUNCE_MILLIS = 400L

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val settingsRepository: SettingsRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val lockedMediaType: MediaType? = savedStateHandle.get<String>("mediaType")?.let { MediaType.valueOf(it) }

    private val _uiState = MutableStateFlow(SearchUiState(lockedMediaType = lockedMediaType))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.apiKey.collect { key ->
                _uiState.update { it.copy(hasApiKey = key != null) }
            }
        }
        viewModelScope.launch {
            combine(showRepository.getAllShows(), movieRepository.getAllMovies()) { shows, movies ->
                shows.map { MediaType.TV to it.tmdbId }.toSet() +
                    movies.map { MediaType.MOVIE to it.tmdbId }.toSet()
            }.collect { ids ->
                _uiState.update { it.copy(libraryItems = ids) }
            }
        }
        loadTrendingFeed()
        loadGenres()
    }

    private fun loadTrendingFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFeed = true) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val feed = provider.getTrendingFeed().map { it.toResultItem() }.filterByLockedType()
                _uiState.update { it.copy(trendingFeed = feed, isLoadingFeed = false) }
            } catch (e: MissingTmdbApiKeyException) {
                _uiState.update { it.copy(isLoadingFeed = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingFeed = false, errorMessageRes = R.string.search_error_feed)
                }
            }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val genres = provider.getGenres(lockedMediaType)
                    .sortedBy { it.name }
                    .map { GenreOption(it.id, it.name) }
                _uiState.update { it.copy(availableGenres = genres) }
            } catch (e: Exception) {
                // Genre filter is a bonus, not core search functionality — fail silently.
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            _uiState.update { it.copy(isSearching = true, errorMessageRes = null) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val results = provider.search(query).map { it.toResultItem() }.filterByLockedType()
                _uiState.update { it.copy(results = results, isSearching = false) }
            } catch (e: MissingTmdbApiKeyException) {
                _uiState.update { it.copy(isSearching = false, hasApiKey = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, errorMessageRes = R.string.search_error_query) }
            }
        }
    }

    fun onGenreFilterApplied(genreIds: Set<Int>) {
        _uiState.update { it.copy(selectedGenreIds = genreIds) }
    }

    private fun List<SearchResultItem>.filterByLockedType(): List<SearchResultItem> =
        lockedMediaType?.let { type -> filter { it.mediaType == type } } ?: this

    fun onAddClicked(item: SearchResultItem) {
        val key = item.mediaType to item.id
        if (key in _uiState.value.pendingItems || key in _uiState.value.libraryItems) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingItems = it.pendingItems + key) }
            try {
                when (item.mediaType) {
                    MediaType.TV -> showRepository.addShowFromTmdb(item.id)
                    MediaType.MOVIE -> movieRepository.addMovieFromTmdb(item.id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessageRes = R.string.search_error_add) }
            } finally {
                _uiState.update { it.copy(pendingItems = it.pendingItems - key) }
            }
        }
    }

    fun onRemoveClicked(item: SearchResultItem) {
        val key = item.mediaType to item.id
        if (key in _uiState.value.pendingItems || key !in _uiState.value.libraryItems) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingItems = it.pendingItems + key) }
            try {
                when (item.mediaType) {
                    MediaType.TV -> showRepository.removeShow(item.id)
                    MediaType.MOVIE -> movieRepository.removeMovie(item.id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessageRes = R.string.search_error_remove) }
            } finally {
                _uiState.update { it.copy(pendingItems = it.pendingItems - key) }
            }
        }
    }

    private suspend fun TmdbSearchResult.toResultItem() = SearchResultItem(
        id = id,
        mediaType = mediaType,
        title = title,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        date = date,
        genreIds = genreIds,
    )

    private suspend fun MediaPreview.toResultItem() = SearchResultItem(
        id = tmdbId,
        mediaType = mediaType,
        title = title,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        date = releaseDate,
        genreIds = genreIds,
    )
}
