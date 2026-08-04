package com.reelia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.reelia.app.domain.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Carries a "open this detail screen" request from a tapped notification (handled in
 * `MainActivity.onCreate`/`onNewIntent`, since neither runs inside the Compose tree) across to
 * the NavGraph once it exists. Modeled on [BottomNavScrollToTop]'s same "latest request as a
 * conflated StateFlow, not a one-shot event" reasoning — the Activity can receive the launching
 * intent before `TimeLineNavGraph` has finished composing, so a zero-replay event would be missed.
 */
object PendingNotificationTarget {
    data class Target(val mediaType: MediaType, val tmdbId: Int)

    private val _target = MutableStateFlow<Target?>(null)
    val target: StateFlow<Target?> = _target.asStateFlow()

    fun set(mediaType: MediaType, tmdbId: Int) {
        _target.value = Target(mediaType, tmdbId)
    }

    fun clear() {
        _target.value = null
    }
}

/** Navigates to the pending notification target's detail screen exactly once, then clears it —
 * call once from the composable hosting [navController]. */
@Composable
fun HandlePendingNotificationTarget(navController: NavHostController) {
    val target by PendingNotificationTarget.target.collectAsStateWithLifecycle()
    LaunchedEffect(target) {
        val current = target ?: return@LaunchedEffect
        when (current.mediaType) {
            MediaType.TV -> navController.navigate(Routes.showDetail(current.tmdbId))
            MediaType.MOVIE -> navController.navigate(Routes.movieDetail(current.tmdbId))
        }
        PendingNotificationTarget.clear()
    }
}
