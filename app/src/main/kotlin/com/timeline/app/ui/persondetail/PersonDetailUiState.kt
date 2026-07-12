package com.timeline.app.ui.persondetail

import androidx.annotation.StringRes

data class PersonDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val photoUrl: String? = null,
    val biography: String = "",
    val birthday: String? = null,
    val placeOfBirth: String? = null,
    @StringRes val errorMessageRes: Int? = null,
)
