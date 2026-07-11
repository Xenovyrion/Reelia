package com.timeline.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.ui.common.components.BarChart
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.StatCard
import com.timeline.app.ui.theme.timeLineTopAppBarColors
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule

private fun StatsScope.label(): String = when (this) {
    StatsScope.ALL -> "Tout"
    StatsScope.SERIES -> "Séries"
    StatsScope.FILMS -> "Films"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistiques") }, colors = timeLineTopAppBarColors()) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsScope.entries.forEachIndexed { index, scope ->
                    SegmentedButton(
                        selected = uiState.scope == scope,
                        onClick = { viewModel.onScopeSelected(scope) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsScope.entries.size),
                        label = { Text(scope.label()) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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

            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader("Par semaine")
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(
                        "Heures regardées, 12 dernières semaines",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = uiState.weeklyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader("Par mois")
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(
                        "Heures regardées, 12 derniers mois",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = uiState.monthlyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}
