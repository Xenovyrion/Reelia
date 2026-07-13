package com.timeline.app.ui.persondetail

import androidx.annotation.StringRes
import com.timeline.app.domain.model.MediaType

data class PersonFilmographyItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val character: String?,
    val year: String?,
)

data class PersonDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val photoUrl: String? = null,
    val biography: String = "",
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val filmography: List<PersonFilmographyItem> = emptyList(),
    @StringRes val errorMessageRes: Int? = null,
)
