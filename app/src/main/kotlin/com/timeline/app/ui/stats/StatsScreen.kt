package com.timeline.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.ui.common.components.StatCard
import com.timeline.app.ui.theme.timeLineTopAppBarColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistiques") }, colors = timeLineTopAppBarColors()) },
    ) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Filled.Schedule,
                value = String.format(Locale.getDefault(), "%.1f", uiState.totalHoursWatched),
                unitLabel = "HEURES",
                caption = "Temps total regardé",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Filled.CheckCircle,
                value = uiState.totalWatchedCount.toString(),
                unitLabel = "TITRES",
                caption = "Épisodes et films vus",
                modifier = Modifier.weight(1f),
            )
        }
    }
}
