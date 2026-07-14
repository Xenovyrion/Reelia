package com.reelia.app.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.R

/** Mounted once at the top of the app content (see MainActivity) — auto-checks GitHub for a
 * newer build once per process, and shows a dismissible banner if one is found. */
@Composable
fun UpdateBanner(viewModel: UpdateViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.checkForUpdateOnce() }

    LaunchedEffect(uiState.downloadedApkUri) {
        uiState.downloadedApkUri?.let { uri ->
            context.startActivity(viewModel.buildInstallIntent(uri))
            viewModel.onInstallLaunched()
        }
    }

    if (uiState.availableUpdate == null) return
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.update_banner_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    if (uiState.errorMessage != null) {
                        stringResource(R.string.update_download_error)
                    } else {
                        stringResource(R.string.update_banner_subtitle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.errorMessage != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (uiState.isDownloading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                TextButton(onClick = viewModel::onDownloadAndInstallClicked) {
                    Text(stringResource(R.string.update_action_download_install))
                }
                TextButton(onClick = viewModel::onDismiss) {
                    Text(stringResource(R.string.update_action_dismiss))
                }
            }
        }
    }
}
