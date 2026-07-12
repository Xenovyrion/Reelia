package com.timeline.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.R
import com.timeline.app.data.metadata.MetadataProviderRegistry
import com.timeline.app.data.remote.tmdb.MissingTmdbApiKeyException
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.SettingsRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaPreview
import com.timeline.app.domain.model.MediaType
import com.timeline.app.domain.model.TmdbSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val settingsRepository: SettingsRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val addedEventChannel = Channel<String>(Channel.BUFFERED)
    val addedEvent: Flow<String> = addedEventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.apiKey.collect { key ->
                _uiState.update { it.copy(hasApiKey = key != null) }
            }
        }
        loadTrendingFeed()
    }

    private fun loadTrendingFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFeed = true) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val feed = provider.getTrendingFeed().map { it.toResultItem() }
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

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isSearching = true, errorMessageRes = null) }
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val results = provider.search(query).map { it.toResultItem() }
                _uiState.update { it.copy(results = results, isSearching = false) }
            } catch (e: MissingTmdbApiKeyException) {
                _uiState.update { it.copy(isSearching = false, hasApiKey = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, errorMessageRes = R.string.search_error_query) }
            }
        }
    }

    fun onAddClicked(item: SearchResultItem) {
        viewModelScope.launch {
            try {
                when (item.mediaType) {
                    MediaType.TV -> showRepository.addShowFromTmdb(item.id)
                    MediaType.MOVIE -> movieRepository.addMovieFromTmdb(item.id)
                }
                addedEventChannel.send(item.title)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessageRes = R.string.search_error_add) }
            }
        }
    }

    private suspend fun TmdbSearchResult.toResultItem() = SearchResultItem(
        id = id,
        mediaType = mediaType,
        title = title,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        date = date,
    )

    private suspend fun MediaPreview.toResultItem() = SearchResultItem(
        id = tmdbId,
        mediaType = mediaType,
        title = title,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        date = releaseDate,
    )
}
