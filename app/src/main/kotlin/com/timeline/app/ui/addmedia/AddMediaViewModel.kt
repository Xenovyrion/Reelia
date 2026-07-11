package com.timeline.app.ui.addmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.remote.tmdb.MissingTmdbApiKeyException
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.data.repository.MovieRepository
import com.timeline.app.data.repository.SearchRepository
import com.timeline.app.data.repository.SettingsRepository
import com.timeline.app.data.repository.ShowRepository
import com.timeline.app.domain.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddMediaViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val settingsRepository: SettingsRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMediaUiState())
    val uiState: StateFlow<AddMediaUiState> = _uiState.asStateFlow()

    private val addedEvents = Channel<Pair<MediaType, Int>>(Channel.BUFFERED)
    val addedEvent = addedEvents.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val hasKey = settingsRepository.apiKey.first() != null
            _uiState.update { it.copy(hasApiKey = hasKey) }
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
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            try {
                val results = searchRepository.searchMulti(query).map { result ->
                    AddMediaResultUi(result = result, posterUrl = imageUrlBuilder.posterUrl(result.posterPath))
                }
                _uiState.update { it.copy(results = results, isSearching = false) }
            } catch (e: MissingTmdbApiKeyException) {
                _uiState.update { it.copy(isSearching = false, hasApiKey = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, errorMessage = "Recherche impossible. Vérifie ta connexion.") }
            }
        }
    }

    fun onResultSelected(resultUi: AddMediaResultUi) {
        val result = resultUi.result
        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true, errorMessage = null) }
            try {
                when (result.mediaType) {
                    MediaType.TV -> showRepository.addShowFromTmdb(result.id)
                    MediaType.MOVIE -> movieRepository.addMovieFromTmdb(result.id)
                }
                addedEvents.send(result.mediaType to result.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Impossible d'ajouter ce titre. Réessaie.") }
            } finally {
                _uiState.update { it.copy(isAdding = false) }
            }
        }
    }
}
