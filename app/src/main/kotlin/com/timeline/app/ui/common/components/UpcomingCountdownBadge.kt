package com.timeline.app.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.timeline.app.R

/** Shared "days until" countdown for upcoming episode/release cards — a big bold day count
 * with a "DAYS" caption underneath for anything more than a day out, otherwise a short label. */
@Composable
fun UpcomingCountdownBadge(daysUntil: Long, modifier: Modifier = Modifier) {
    when {
        daysUntil < 0 -> Text(
            stringResource(R.string.days_until_aired),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
        daysUntil == 0L -> Text(
            stringResource(R.string.days_until_today),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
        daysUntil == 1L -> Text(
            stringResource(R.string.days_until_tomorrow),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
        else -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
            Text(
                daysUntil.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.days_until_unit_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
