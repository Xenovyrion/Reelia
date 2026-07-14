package com.reelia.app.ui.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.R
import com.reelia.app.data.metadata.MetadataProviderRegistry
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaPreview
import com.reelia.app.domain.model.WatchProviderOption
import com.reelia.app.ui.common.components.CastRowItem
import com.reelia.app.ui.common.components.WatchProviderRowItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ShowPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val metadataProviderRegistry: MetadataProviderRegistry,
    private val showRepository: ShowRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val tmdbId: Int = checkNotNull(savedStateHandle["tmdbId"])

    private val _uiState = MutableStateFlow(ShowPreviewUiState())
    val uiState: StateFlow<ShowPreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val provider = metadataProviderRegistry.activeProvider.first()
                val preview = provider.getShowPreview(tmdbId)
                _uiState.value = preview.toUiState()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = R.string.preview_error_load_show)
                }
            }
        }
    }

    fun onAddClicked(onAdded: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true) }
            try {
                showRepository.addShowFromTmdb(tmdbId)
                _uiState.update { it.copy(isAdding = false, added = true) }
                delay(500)
                onAdded()
            } catch (e: Exception) {
                _uiState.update { it.copy(isAdding = false, errorMessageRes = R.string.preview_error_add_show) }
            }
        }
    }

    private suspend fun MediaPreview.toUiState(): ShowPreviewUiState = ShowPreviewUiState(
        isLoading = false,
        title = title,
        overview = overview,
        posterUrl = imageUrlBuilder.posterUrl(posterPath),
        backdropUrl = imageUrlBuilder.backdropUrl(backdropPath),
        firstAirDate = releaseDate,
        numberOfSeasons = numberOfSeasons,
        voteAverage = voteAverage,
        genreNames = genreNames,
        networkNames = networkNames,
        cast = cast.map {
            CastRowItem(
                personId = it.id,
                name = it.name,
                character = it.character,
                photoUrl = imageUrlBuilder.posterUrl(it.profilePath, size = "w185"),
            )
        },
        watchProvidersFlatrate = watchProviders?.flatrate.orEmpty().toRowItems(),
        watchProvidersRent = watchProviders?.rent.orEmpty().toRowItems(),
        watchProvidersBuy = watchProviders?.buy.orEmpty().toRowItems(),
    )

    private suspend fun List<WatchProviderOption>.toRowItems(): List<WatchProviderRowItem> =
        map { WatchProviderRowItem(name = it.providerName, logoUrl = imageUrlBuilder.posterUrl(it.logoPath, size = "w92")) }
}
