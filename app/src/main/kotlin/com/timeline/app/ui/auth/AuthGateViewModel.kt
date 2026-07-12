package com.timeline.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    /** Null while Firebase hasn't reported an initial auth state yet, then true/false. */
    val isSignedIn: StateFlow<Boolean?> = authRepository.currentUser
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
