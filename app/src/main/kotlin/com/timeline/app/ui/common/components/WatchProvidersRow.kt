package com.timeline.app.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.timeline.app.R

data class WatchProviderRowItem(val name: String, val logoUrl: String?)

@Composable
fun WatchProvidersRow(
    flatrate: List<WatchProviderRowItem>,
    rent: List<WatchProviderRowItem>,
    buy: List<WatchProviderRowItem>,
) {
    if (flatrate.isEmpty() && rent.isEmpty() && buy.isEmpty()) {
        Text(
            stringResource(R.string.watch_providers_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column {
        ProviderGroup(stringResource(R.string.watch_providers_streaming), flatrate)
        ProviderGroup(stringResource(R.string.watch_providers_rent), rent)
        ProviderGroup(stringResource(R.string.watch_providers_buy), buy)
    }
}

@Composable
private fun ProviderGroup(label: String, providers: List<WatchProviderRowItem>) {
    if (providers.isEmpty()) return
    Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
    LazyRow {
        items(providers) { provider ->
            ProviderBadge(provider, modifier = Modifier.padding(end = 12.dp))
        }
    }
}

@Composable
private fun ProviderBadge(provider: WatchProviderRowItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            AsyncImage(
                model = provider.logoUrl,
                contentDescription = provider.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Text(
            provider.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
