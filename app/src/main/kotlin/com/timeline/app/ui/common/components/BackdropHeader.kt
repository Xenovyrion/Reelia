package com.timeline.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
 * caller's content below this header instead.
 */
@Composable
fun BackdropHeader(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.32f,
    onBack: () -> Unit,
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
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.backdrop_back_content_description),
                tint = Color.White,
            )
        }
    }
}
