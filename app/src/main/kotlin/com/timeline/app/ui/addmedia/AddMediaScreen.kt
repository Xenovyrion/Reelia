package com.timeline.app.ui.addmedia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun AddMediaScreen(
    onBack: () -> Unit,
    onAdded: (MediaType, Int) -> Unit,
    viewModel: AddMediaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.addedEvent.collect { (mediaType, id) -> onAdded(mediaType, id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = timeLineTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (!uiState.hasApiKey) {
                Text("Ajoute d'abord ta clé API TMDB dans Réglages pour pouvoir chercher une série ou un film.")
                return@Column
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Titre d'une série ou d'un film") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            uiState.errorMessage?.let { Text(it) }

            if (uiState.isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(uiState.results, key = { "${it.result.mediaType}_${it.result.id}" }) { resultUi ->
                        ListItem(
                            headlineContent = { Text(resultUi.result.title) },
                            supportingContent = { Text(resultUi.result.date.orEmpty()) },
                            leadingContent = {
                                AsyncImage(
                                    model = resultUi.posterUrl,
                                    contentDescription = null,
                                    modifier = Modifier.height(64.dp),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !uiState.isAdding) { viewModel.onResultSelected(resultUi) },
                        )
                    }
                }
            }
        }
    }
}
