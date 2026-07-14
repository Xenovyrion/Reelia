package com.reelia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Compose Navigation's saveState/restoreState on the bottom-tab items intentionally preserves
 * each tab's scroll position when switching away and back — normally desirable, but not when the
 * user deliberately taps a tab, which should always land back at the top like a fresh view.
 *
 * Modeled as the *latest* request (a conflated state, not a one-shot event) rather than a
 * `SharedFlow` with no replay: a bottom-tab tap fires before the destination composable — and
 * its collector — even exists, so a zero-replay event would simply be missed every time. A
 * `StateFlow` always hands its current value to a new collector, so the request is never lost
 * regardless of exactly when the target screen finishes composing.
 */
object BottomNavScrollToTop {
    private val _lastRequest = MutableStateFlow<Pair<String, Long>?>(null)
    val lastRequest: StateFlow<Pair<String, Long>?> = _lastRequest.asStateFlow()

    fun requestScrollToTop(route: String) {
        _lastRequest.value = route to System.nanoTime()
    }
}

/**
 * Scrolls to the top exactly once per bottom-tab tap on [route] — not on every recomposition
 * that happens to observe the same still-current request, which would otherwise also fire when
 * simply returning from a detail screen via the back button (where the scroll position must be
 * kept, not reset). [rememberSaveable] survives the same tab-switch teardown/rebuild the request
 * itself needs to survive, so a request already handled before this screen was last torn down is
 * correctly recognized as "already consumed" instead of re-triggering.
 */
@Composable
fun ScrollToTopOnTabReselect(route: String, onScrollToTop: suspend () -> Unit) {
    val request by BottomNavScrollToTop.lastRequest.collectAsStateWithLifecycle()
    var lastConsumed by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(request) {
        val (requestedRoute, timestamp) = request ?: return@LaunchedEffect
        if (requestedRoute == route && timestamp != lastConsumed) {
            lastConsumed = timestamp
            onScrollToTop()
        }
    }
}
