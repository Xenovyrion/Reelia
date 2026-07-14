package com.reelia.app.ui.update

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.data.update.AppUpdate
import com.reelia.app.data.update.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUiState(
    val isChecking: Boolean = false,
    val hasChecked: Boolean = false,
    val availableUpdate: AppUpdate? = null,
    val isDownloading: Boolean = false,
    val downloadedApkUri: Uri? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var hasCheckedThisSession = false

    /** Checks once per app process — the banner mounts once at the top of the Compose tree,
     * so re-composition (rotation, nav) doesn't refire the network call. */
    fun checkForUpdateOnce() {
        if (hasCheckedThisSession) return
        hasCheckedThisSession = true
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            val update = appUpdateRepository.checkForUpdate()
            _uiState.update { it.copy(isChecking = false, hasChecked = true, availableUpdate = update) }
        }
    }

    fun onDownloadAndInstallClicked() {
        val update = _uiState.value.availableUpdate ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, errorMessage = null) }
            try {
                val uri = appUpdateRepository.downloadUpdate(update)
                _uiState.update { it.copy(isDownloading = false, downloadedApkUri = uri) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDownloading = false, errorMessage = e.message) }
            }
        }
    }

    fun buildInstallIntent(apkUri: Uri): Intent = appUpdateRepository.buildInstallIntent(apkUri)

    /** Called once the install intent has actually been launched, so it doesn't refire on
     * recomposition. */
    fun onInstallLaunched() {
        _uiState.update { it.copy(downloadedApkUri = null) }
    }

    fun onDismiss() {
        _uiState.update { it.copy(availableUpdate = null) }
    }
}
