package com.reelia.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.reelia.app.R
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.common.components.GenreFilterBottomSheet
import com.reelia.app.ui.common.components.SectionHeader
import com.reelia.app.ui.common.format.toYearOrNull
import com.reelia.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onItemClick: (MediaType, Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    val titleRes = when (uiState.lockedMediaType) {
        MediaType.TV -> R.string.search_title_series
        MediaType.MOVIE -> R.string.search_title_films
        null -> R.string.search_title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                colors = timeLineTopAppBarColors(),
                actions = {
                    if (uiState.availableGenres.isNotEmpty()) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = stringResource(R.string.action_filter_content_description),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.search_field_label)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.search_clear_content_description),
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )

            if (!uiState.hasApiKey) {
                Text(
                    stringResource(R.string.search_missing_api_key_message),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                return@Column
            }

            uiState.errorMessageRes?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }

            val isLoading = if (uiState.query.isBlank()) uiState.isLoadingFeed else uiState.isSearching
            val items = uiState.displayedItems
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.search_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val sectionTitleRes = if (uiState.query.isBlank()) {
                    R.string.search_trending_section_title
                } else {
                    R.string.search_results_section_title
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { SectionHeader(stringResource(sectionTitleRes), modifier = Modifier.padding(16.dp)) }
                    items(items.chunked(3), key = { row -> "${row.first().mediaType}_${row.first().id}" }) { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                            row.forEach { item ->
                                SearchResultCard(
                                    item = item,
                                    isPending = (item.mediaType to item.id) in uiState.pendingItems,
                                    isInLibrary = (item.mediaType to item.id) in uiState.libraryItems,
                                    onClick = { onItemClick(item.mediaType, item.id) },
                                    onAddClick = { viewModel.onAddClicked(item) },
                                    onRemoveClick = { viewModel.onRemoveClicked(item) },
                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                )
                            }
                            repeat(3 - row.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        GenreFilterBottomSheet(
            availableGenres = uiState.availableGenres,
            selectedGenreIds = uiState.selectedGenreIds,
            onApply = { genreIds ->
                viewModel.onGenreFilterApplied(genreIds)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    isPending: Boolean,
    isInLibrary: Boolean,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
            )
            Surface(
                shape = CircleShape,
                // A muted tertiary once added — reusing the same primary color as "add" would
                // make the checkmark look like just another clickable add button.
                color = if (isInLibrary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clickable(
                        enabled = !isPending,
                        onClick = if (isInLibrary) onRemoveClick else onAddClick,
                    ),
            ) {
                if (isPending) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(7.dp),
                    )
                } else if (isInLibrary) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.search_remove_content_description),
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(5.dp),
                    )
                } else {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.search_add_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(5.dp),
                    )
                }
            }
        }
        Text(
            item.title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Text(
            item.date.toYearOrNull().orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
