package com.reelia.app.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Compose Navigation's saveState/restoreState on the bottom-tab items intentionally preserves
 * each tab's scroll position when switching away and back — normally desirable, but not when the
 * user deliberately taps a tab, which should always land back at the top like a fresh view.
 * Bottom-tab screens collect [events] keyed to their own route and scroll themselves to the top.
 */
object BottomNavScrollToTop {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun requestScrollToTop(route: String) {
        _events.tryEmit(route)
    }
}
