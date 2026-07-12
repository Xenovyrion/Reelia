package com.timeline.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.common.format.toYearOrNull
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onItemClick: (MediaType, Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedConfirmationFormat = stringResource(R.string.search_add_confirmation)

    LaunchedEffect(Unit) {
        viewModel.addedEvent.collect { title ->
            snackbarHostState.showSnackbar(String.format(addedConfirmationFormat, title))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.search_title)) }, colors = timeLineTopAppBarColors()) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.search_field_label)) },
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(uiState.displayedItems, key = { "${it.mediaType}_${it.id}" }) { item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.date.toYearOrNull().orEmpty()) },
                            leadingContent = {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = null,
                                    modifier = Modifier.height(64.dp),
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.onAddClicked(item) }) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.search_add_content_description),
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item.mediaType, item.id) },
                        )
                    }
                }
            }
        }
    }
}
