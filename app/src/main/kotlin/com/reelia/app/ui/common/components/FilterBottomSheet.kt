package com.reelia.app.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.reelia.app.R
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.domain.model.displayLabel

data class GenreOption(val id: Int, val name: String)

enum class LibrarySortOption { STATUS, ALPHABETICAL, GENRE, RECENTLY_ADDED, RECENTLY_WATCHED }

private val DEFAULT_SORT_OPTION = LibrarySortOption.STATUS

@Composable
private fun LibrarySortOption.label(): String = stringResource(
    when (this) {
        LibrarySortOption.STATUS -> R.string.library_sort_status
        LibrarySortOption.ALPHABETICAL -> R.string.library_sort_alphabetical
        LibrarySortOption.GENRE -> R.string.library_sort_genre
        LibrarySortOption.RECENTLY_ADDED -> R.string.library_sort_recently_added
        LibrarySortOption.RECENTLY_WATCHED -> R.string.library_sort_recently_watched
    },
)

/** Filter and sort share one sheet (opened from the same toolbar button) rather than two nearly
 * identical entry points — sort is applied together with the filters on "Appliquer" rather than
 * instantly, so the whole set of choices is confirmed as one action. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedStatuses: Set<WatchStatus>,
    availableGenres: List<GenreOption>,
    selectedGenreIds: Set<Int>,
    sortOption: LibrarySortOption,
    onApply: (Set<WatchStatus>, Set<Int>, LibrarySortOption) -> Unit,
    onDismiss: () -> Unit,
) {
    var statuses by remember { mutableStateOf(selectedStatuses) }
    var genreIds by remember { mutableStateOf(selectedGenreIds) }
    var sort by remember { mutableStateOf(sortOption) }

    // Fixed max height + an internally scrolling content column, so the Reset/Appliquer row
    // always stays reachable at the bottom instead of requiring a scroll past every chip section.
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.library_sort_content_description), style = MaterialTheme.typography.titleMedium)
                FlowRow(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                    LibrarySortOption.entries.forEach { option ->
                        FilterChip(
                            selected = sort == option,
                            onClick = { sort = option },
                            label = { Text(option.label()) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                }
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
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                TextButton(onClick = {
                    statuses = emptySet()
                    genreIds = emptySet()
                    sort = DEFAULT_SORT_OPTION
                    onApply(statuses, genreIds, sort)
                }) {
                    Text(stringResource(R.string.filter_reset_button))
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onApply(statuses, genreIds, sort) }) {
                    Text(stringResource(R.string.filter_apply_button))
                }
            }
        }
    }
}

/** Genre-only filter sheet for the Search screen — unlike [FilterBottomSheet], there's no
 * watch-status dimension to filter by since search results aren't in the library yet. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GenreFilterBottomSheet(
    availableGenres: List<GenreOption>,
    selectedGenreIds: Set<Int>,
    onApply: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var genreIds by remember { mutableStateOf(selectedGenreIds) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            ) {
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
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                TextButton(onClick = {
                    genreIds = emptySet()
                    onApply(genreIds)
                }) {
                    Text(stringResource(R.string.filter_reset_button))
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onApply(genreIds) }) {
                    Text(stringResource(R.string.filter_apply_button))
                }
            }
        }
    }
}
