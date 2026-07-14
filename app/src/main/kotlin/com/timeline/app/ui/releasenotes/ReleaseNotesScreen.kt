package com.timeline.app.ui.releasenotes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.data.releasenotes.ReleaseNoteCategory
import com.timeline.app.data.releasenotes.ReleaseNoteItem
import com.timeline.app.data.releasenotes.ReleaseNoteVersion
import com.timeline.app.ui.common.components.BackButton
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesScreen(onBack: () -> Unit, viewModel: ReleaseNotesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.release_notes_title)) },
                navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) },
                colors = timeLineTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val state = uiState) {
                is ReleaseNotesUiState.Loading -> CircularProgressIndicator()
                is ReleaseNotesUiState.Error -> Text(
                    stringResource(R.string.release_notes_error),
                    style = MaterialTheme.typography.bodyMedium,
                )
                is ReleaseNotesUiState.Loaded -> {
                    state.versions.forEachIndexed { index, version ->
                        ReleaseNoteVersionCard(version = version, isCurrent = index == 0)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseNoteVersionCard(version: ReleaseNoteVersion, isCurrent: Boolean) {
    var expanded by remember(version.version) { mutableStateOf(isCurrent) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.release_notes_version_format, version.version), style = MaterialTheme.typography.titleMedium)
                        if (isCurrent) {
                            Surface(color = StatusWatchingCompleted.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
                                Text(
                                    stringResource(R.string.release_notes_current_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusWatchingCompleted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    if (version.dateLabel.isNotBlank()) {
                        Text(
                            version.dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val grouped = version.items.groupBy { it.category }
                    listOf(
                        ReleaseNoteCategory.FEATURE,
                        ReleaseNoteCategory.FIX,
                        ReleaseNoteCategory.IMPROVEMENT,
                        ReleaseNoteCategory.OTHER,
                    ).forEach { category ->
                        val items = grouped[category].orEmpty()
                        if (items.isNotEmpty()) {
                            ReleaseNoteCategorySection(category, items)
                        }
                    }
                }
            }
        }
    }
}

private data class CategoryStyle(val icon: ImageVector, val color: Color, val labelRes: Int)

@Composable
private fun categoryStyle(category: ReleaseNoteCategory): CategoryStyle = when (category) {
    ReleaseNoteCategory.FEATURE -> CategoryStyle(Icons.Filled.AutoAwesome, StatusWatchingCompleted, R.string.release_notes_category_feature)
    ReleaseNoteCategory.FIX -> CategoryStyle(Icons.Filled.BugReport, StatusFavorite, R.string.release_notes_category_fix)
    ReleaseNoteCategory.IMPROVEMENT -> CategoryStyle(Icons.Filled.Build, StatusPlanned, R.string.release_notes_category_improvement)
    ReleaseNoteCategory.OTHER -> CategoryStyle(Icons.Filled.Info, StatusWantToWatch, R.string.release_notes_category_other)
}

@Composable
private fun ReleaseNoteCategorySection(category: ReleaseNoteCategory, items: List<ReleaseNoteItem>) {
    val style = categoryStyle(category)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(18.dp))
            Text(stringResource(style.labelRes), style = MaterialTheme.typography.labelLarge, color = style.color)
        }
        items.forEach { item ->
            Text(
                "•  ${item.text}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp),
            )
        }
    }
}
