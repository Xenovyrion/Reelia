package com.timeline.app.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.timeline.app.R
import com.timeline.app.domain.model.WatchStatus
import com.timeline.app.domain.model.displayLabel

data class GenreOption(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedStatuses: Set<WatchStatus>,
    availableGenres: List<GenreOption>,
    selectedGenreIds: Set<Int>,
    onApply: (Set<WatchStatus>, Set<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var statuses by remember { mutableStateOf(selectedStatuses) }
    var genreIds by remember { mutableStateOf(selectedGenreIds) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.filter_status_label), style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                WatchStatus.entries.forEach { status ->
                    FilterChip(
                        selected = status in statuses,
                        onClick = {
                            statuses = if (status in statuses) statuses - status else statuses + status
                        },
                        label = { Text(status.displayLabel()) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                    )
                }
            }
            if (availableGenres.isNotEmpty()) {
                Text(stringResource(R.string.filter_genre_label), style = MaterialTheme.typography.titleMedium)
                FlowRow(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                    availableGenres.forEach { genre ->
                        FilterChip(
                            selected = genre.id in genreIds,
                            onClick = {
                                genreIds = if (genre.id in genreIds) genreIds - genre.id else genreIds + genre.id
                            },
                            label = { Text(genre.name) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    statuses = emptySet()
                    genreIds = emptySet()
                }) {
                    Text(stringResource(R.string.filter_reset_button))
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onApply(statuses, genreIds) }) {
                    Text(stringResource(R.string.filter_apply_button))
                }
            }
        }
    }
}
