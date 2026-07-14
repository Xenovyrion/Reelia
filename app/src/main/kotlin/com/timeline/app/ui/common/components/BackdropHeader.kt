package com.timeline.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.timeline.app.R

/**
 * Full-bleed hero image over the top of a detail screen, with a gradient scrim fading into
 * the background so it blends seamlessly into the content below. Title is intentionally NOT
 * overlaid on the image (legibility varies too much per source image) — render it in the
 * caller's content below this header instead. [onDelete], when provided, shows a matching
 * pill button at the opposite corner (e.g. "remove from library" on Show/Movie detail).
 */
@Composable
fun BackdropHeader(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.32f,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val heightDp = (LocalConfiguration.current.screenHeightDp * heightFraction).dp
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxWidth().height(heightDp)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(heightDp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1f to backgroundColor,
                        ),
                    ),
                ),
        )
        BackButton(
            onClick = onBack,
            onImage = true,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        )
        if (onDelete != null) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(38.dp),
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.detail_remove_content_description),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
