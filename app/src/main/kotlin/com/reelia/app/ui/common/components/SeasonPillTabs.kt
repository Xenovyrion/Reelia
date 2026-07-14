package com.reelia.app.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reelia.app.ui.theme.OnStatusColor
import com.reelia.app.ui.theme.StatusWantToWatch

data class SeasonPillItem(val seasonNumber: Int, val label: String)

/**
 * Pill-style season switcher (rounded, active pill filled in periwinkle) — replaces the
 * plain TabRow used for Detail's season tabs before this redesign.
 */
@Composable
fun SeasonPillTabs(
    seasons: List<SeasonPillItem>,
    selectedSeasonNumber: Int,
    onSeasonSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(seasons, key = { it.seasonNumber }) { season ->
            val isSelected = season.seasonNumber == selectedSeasonNumber
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) StatusWantToWatch else MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.clickable { onSeasonSelected(season.seasonNumber) },
            ) {
                Text(
                    text = season.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) OnStatusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
