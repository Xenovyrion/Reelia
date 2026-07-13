package com.timeline.app.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val TOP_TOAST_DURATION_MILLIS = 2500L

/** Confirmation banner anchored to the top of the screen, unlike Material's default
 * bottom [androidx.compose.material3.SnackbarHost] — needed on screens with a text field
 * (e.g. Search) where the keyboard covers the bottom of the screen and hides a bottom snackbar. */
@Composable
fun TopToastHost(events: Flow<String>, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        events.collect {
            message = it
        }
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(TOP_TOAST_DURATION_MILLIS)
            message = null
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Text(message.orEmpty(), modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}
