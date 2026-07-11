package com.timeline.app.ui.upnext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.timeline.app.ui.theme.timeLineTopAppBarColors

/**
 * Placeholder for the "next episode air date" calendar. Real data (backed by
 * TrackedShowEntity.nextEpisodeToAirDate, already cached from TMDB) lands in a later iteration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpNextScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("À venir") }, colors = timeLineTopAppBarColors()) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Bientôt disponible : le calendrier des prochains épisodes.")
        }
    }
}
