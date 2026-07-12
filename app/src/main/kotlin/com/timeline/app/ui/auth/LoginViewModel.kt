package com.timeline.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onToggleMode() {
        _uiState.update {
            it.copy(isSignUpMode = !it.isSignUpMode, email = "", password = "", errorMessage = null)
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                if (state.isSignUpMode) {
                    authRepository.signUp(state.email, state.password)
                } else {
                    authRepository.signIn(state.email, state.password)
                }
                // A successful sign-in/up updates AuthRepository.currentUser via Firebase's
                // own auth-state listener; the observing root composable reacts to that and
                // swaps away from LoginScreen — no local navigation call needed here.
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                authRepository.signInWithGoogleIdToken(idToken)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Credential Manager itself failed (user cancelled, no Google account on device, etc.) —
     * surfaced the same way as a Firebase Auth error. */
    fun onGoogleSignInFailed(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
