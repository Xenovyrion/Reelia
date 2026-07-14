package com.reelia.app.ui.announcement

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelia.app.R

/** Mounted once at the top of the app content (see MainActivity), alongside [com.reelia.app.ui.update.UpdateBanner].
 * Checks docs/announcement.json once per process. A "normal" announcement shows as a dismissible
 * banner; one marked `important` in the JSON shows as a blocking dialog instead. Either way, once
 * dismissed its id is remembered so the same message never reappears — only a new id does. */
@Composable
fun AnnouncementBanner(viewModel: AnnouncementViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.checkOnce() }

    val announcement = uiState.announcement ?: return

    if (announcement.important) {
        AlertDialog(
            onDismissRequest = viewModel::onDismiss,
            title = { Text(stringResource(R.string.announcement_dialog_title)) },
            text = { Text(announcement.message) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismiss) {
                    Text(stringResource(R.string.announcement_dialog_confirm))
                }
            },
        )
    } else {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    announcement.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
                IconButton(onClick = viewModel::onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.announcement_dismiss_content_description),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
