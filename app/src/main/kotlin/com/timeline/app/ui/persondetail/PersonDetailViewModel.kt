package com.timeline.app.ui.persondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.R
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.common.format.toYearOrNull
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
                // TMDB biographies are rarely translated outside English — fall back to the
                // English one rather than show an empty biography for most non-English users.
                val biography = details.biography.ifBlank {
                    runCatching { tmdbApi.getPersonDetails(personId, language = "en-US") }
                        .getOrNull()
                        ?.biography
                        .orEmpty()
                }

                val credits = runCatching { tmdbApi.getPersonCombinedCredits(personId) }
                    .getOrNull()
                val filmography = credits?.cast
                    .orEmpty()
                    .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                    .distinctBy { it.id to it.mediaType }
                    .sortedByDescending { (it.releaseDate ?: it.firstAirDate).orEmpty() }
                    .mapNotNull { credit ->
                        val title = credit.title ?: credit.name ?: return@mapNotNull null
                        PersonFilmographyItem(
                            id = credit.id,
                            mediaType = if (credit.mediaType == "movie") MediaType.MOVIE else MediaType.TV,
                            title = title,
                            posterUrl = imageUrlBuilder.posterUrl(credit.posterPath, size = "w185"),
                            character = credit.character?.takeIf { it.isNotBlank() },
                            year = (credit.releaseDate ?: credit.firstAirDate).toYearOrNull(),
                        )
                    }

                _uiState.value = PersonDetailUiState(
                    isLoading = false,
                    name = details.name,
                    photoUrl = imageUrlBuilder.posterUrl(details.profilePath, size = "w185"),
                    biography = biography,
                    birthday = details.birthday,
                    deathday = details.deathday,
                    placeOfBirth = details.placeOfBirth,
                    filmography = filmography,
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = R.string.person_detail_error_load)
                }
            }
        }
    }
}
