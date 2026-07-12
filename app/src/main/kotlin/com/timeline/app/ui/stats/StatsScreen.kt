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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.ui.common.components.BarChart
import com.timeline.app.ui.common.components.CircularProgressRing
import com.timeline.app.ui.common.components.GenreProgressBar
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.components.StatCard
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.theme.timeLineTopAppBarColors
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment

@Composable
private fun StatsScope.label(): String = stringResource(
    when (this) {
        StatsScope.ALL -> R.string.stats_scope_all
        StatsScope.SERIES -> R.string.stats_scope_series
        StatsScope.FILMS -> R.string.stats_scope_films
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.stats_title)) }, colors = timeLineTopAppBarColors()) },
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
                    icon = Icons.Filled.CheckCircle,
                    value = uiState.totalWatchedCount.toString(),
                    unitLabel = stringResource(R.string.stats_titles_unit_label),
                    caption = stringResource(R.string.stats_titles_caption),
                    accentColor = StatusWatchingCompleted,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Filled.Schedule,
                    value = String.format(Locale.getDefault(), "%.1f", uiState.totalHoursWatched),
                    unitLabel = stringResource(R.string.stats_hours_unit_label),
                    caption = stringResource(R.string.stats_hours_caption),
                    accentColor = StatusPlanned,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Filled.CheckCircle,
                    value = uiState.completedCount.toString(),
                    unitLabel = stringResource(R.string.stats_completed_unit_label),
                    caption = stringResource(R.string.stats_completed_caption),
                    accentColor = StatusFavorite,
                    modifier = Modifier.weight(1f),
                )
            }

            Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressRing(
                            progress = uiState.completedFraction,
                            size = 56.dp,
                            strokeWidth = 6.dp,
                            color = StatusWatchingCompleted,
                        )
                        Text(
                            "${(uiState.completedFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        stringResource(R.string.stats_completion_ring_caption),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }

            if (uiState.genreBreakdown.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    SectionHeader(stringResource(R.string.stats_genre_section_title))
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        uiState.genreBreakdown.forEach { genre ->
                            GenreProgressBar(item = genre, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader(stringResource(R.string.stats_weekly_section_title))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(
                        stringResource(R.string.stats_weekly_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = uiState.weeklyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader(stringResource(R.string.stats_monthly_section_title))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(
                        stringResource(R.string.stats_monthly_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BarChart(entries = uiState.monthlyChart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}
