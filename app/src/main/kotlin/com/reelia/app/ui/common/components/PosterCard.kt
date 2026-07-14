package com.reelia.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.reelia.app.domain.model.WatchStatus
import com.reelia.app.domain.model.displayLabel
import com.reelia.app.domain.model.statusColor
import com.reelia.app.ui.theme.OnStatusColor
import com.reelia.app.ui.theme.StatusFavorite

@Composable
fun PosterCard(
    title: String,
    posterUrl: String?,
    status: WatchStatus,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    isFavorite: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = posterUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = status.statusColor(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp),
        ) {
            Text(
                text = status.displayLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OnStatusColor,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        if (progress != null) {
            CircularProgressRing(
                progress = progress,
                color = status.statusColor(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            )
        }
        if (isFavorite) {
            Surface(
                shape = CircleShape,
                color = StatusFavorite,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(22.dp),
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = OnStatusColor,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
    }
}
