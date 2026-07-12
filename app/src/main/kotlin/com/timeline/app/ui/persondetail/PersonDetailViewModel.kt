package com.timeline.app.ui.persondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.R
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tmdbApi: TmdbApi,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val personId: Int = checkNotNull(savedStateHandle["personId"])

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val details = tmdbApi.getPersonDetails(personId)
                _uiState.value = PersonDetailUiState(
                    isLoading = false,
                    name = details.name,
                    photoUrl = imageUrlBuilder.posterUrl(details.profilePath, size = "w185"),
                    biography = details.biography,
                    birthday = details.birthday,
                    placeOfBirth = details.placeOfBirth,
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = R.string.person_detail_error_load)
                }
            }
        }
    }
}
