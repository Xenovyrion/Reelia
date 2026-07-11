package com.timeline.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val storedApiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    var apiKeyInput by remember { mutableStateOf("") }

    LaunchedEffect(storedApiKey) {
        storedApiKey?.let { apiKeyInput = it }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Réglages") }, colors = timeLineTopAppBarColors()) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "TimeLine utilise TMDB (The Movie Database) pour récupérer les infos des séries et films. " +
                    "Crée un compte gratuit sur themoviedb.org, puis génère une clé API (v3 auth) dans " +
                    "les paramètres de ton compte, et colle-la ci-dessous.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Clé API TMDB") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.onApiKeySubmitted(apiKeyInput) }) {
                Text("Enregistrer")
            }
        }
    }
}
