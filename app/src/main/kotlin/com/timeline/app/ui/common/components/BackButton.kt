package com.timeline.app.ui.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.timeline.app.R

/** Back-navigation button used on every screen instead of a bare icon — a small pill behind
 * the arrow gives it a floating, tappable feel and matches the badge/chip language used
 * elsewhere (status pills, episode codes). [onImage] switches to a translucent dark pill with
 * a white icon for use over a hero image (see [BackdropHeader]) instead of the default subtle
 * surface tint for flat backgrounds (top bars). */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier, onImage: Boolean = false) {
    Surface(
        shape = CircleShape,
        color = if (onImage) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.size(38.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.backdrop_back_content_description),
                tint = if (onImage) Color.White else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
