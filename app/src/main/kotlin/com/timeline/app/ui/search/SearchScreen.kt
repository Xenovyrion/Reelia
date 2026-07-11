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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onItemClick: (MediaType, Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rechercher") }, colors = timeLineTopAppBarColors()) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Titre d'une série ou d'un film") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )

            if (!uiState.hasApiKey) {
                Text(
                    "Ajoute d'abord ta clé API TMDB dans Réglages pour pouvoir chercher une série ou un film.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                return@Column
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
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
                            supportingContent = { Text(item.date.orEmpty()) },
                            leadingContent = {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = null,
                                    modifier = Modifier.height(64.dp),
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.onAddClicked(item) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Ajouter")
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
