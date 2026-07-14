package com.reelia.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.auth.AuthRepository
import com.reelia.app.data.sync.FirestoreSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
) : ViewModel() {

    /** Null while Firebase hasn't reported an initial auth state yet, then true/false. */
    val isSignedIn: StateFlow<Boolean?> = authRepository.currentUser
        .map { it != null }
        .onEach { signedIn ->
            if (signedIn) {
                firestoreSyncRepository.startListening()
                viewModelScope.launch { firestoreSyncRepository.pushPendingChanges() }
            } else {
                firestoreSyncRepository.stopListening()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
