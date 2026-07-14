package com.timeline.app.ui.announcement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.app.data.announcement.Announcement
import com.timeline.app.data.announcement.AnnouncementRepository
import com.timeline.app.data.local.prefs.AnnouncementPreferenceStore
import com.timeline.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnnouncementUiState(val announcement: Announcement? = null)

@HiltViewModel
class AnnouncementViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val announcementPreferenceStore: AnnouncementPreferenceStore,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementUiState())
    val uiState: StateFlow<AnnouncementUiState> = _uiState.asStateFlow()

    private var hasCheckedThisSession = false

    /** Checks once per app process, same reasoning as [com.timeline.app.ui.update.UpdateViewModel]. */
    fun checkOnce() {
        if (hasCheckedThisSession) return
        hasCheckedThisSession = true
        viewModelScope.launch {
            val language = settingsRepository.language.first()
            val announcement = announcementRepository.fetchAnnouncement(language) ?: return@launch
            val lastSeenId = announcementPreferenceStore.lastSeenId.first()
            if (announcement.id != lastSeenId) {
                _uiState.update { it.copy(announcement = announcement) }
            }
        }
    }

    fun onDismiss() {
        val id = _uiState.value.announcement?.id ?: return
        viewModelScope.launch { announcementPreferenceStore.markSeen(id) }
        _uiState.update { it.copy(announcement = null) }
    }
}
