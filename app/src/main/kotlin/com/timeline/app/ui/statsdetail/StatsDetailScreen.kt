package com.timeline.app.ui.statsdetail

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.common.components.BackButton
import com.timeline.app.ui.common.components.MediaListRow
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailScreen(
    onBack: () -> Unit,
    onItemClick: (MediaType, Int) -> Unit,
    viewModel: StatsDetailViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.title) },
                navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) },
                colors = timeLineTopAppBarColors(),
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Text(
                stringResource(R.string.chart_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(padding).padding(16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(items, key = { "${it.mediaType}_${it.id}" }) { item ->
                    MediaListRow(
                        title = item.title,
                        subtitle = null,
                        posterUrl = item.posterUrl,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { onItemClick(item.mediaType, item.id) },
                    )
                }
            }
        }
    }
}
