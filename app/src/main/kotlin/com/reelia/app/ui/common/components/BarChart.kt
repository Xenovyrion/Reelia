package com.reelia.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reelia.app.R
import java.util.Locale
import kotlin.math.roundToInt

data class BarChartEntry(val label: String, val value: Float)

private val YAxisWidth = 26.dp
private val YAxisSpacing = 6.dp

/** Bar chart built from layout primitives (no charting dependency needed for a few bars) with a
 * numeric y-axis alongside a fixed set of x-axis labels — callers backfill zero-value entries for
 * empty periods so the axis stays stable instead of only showing periods that have data. Bars are
 * tappable: since the y-axis only has 3 gridlines, tapping a bar shows its exact value below the
 * chart rather than making the reader estimate it visually. */
@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    maxBarHeight: Dp = 120.dp,
) {
    if (entries.isEmpty()) {
        Text(
            stringResource(R.string.chart_no_data),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    val rawMax = entries.maxOf { it.value }
    val axisMax = if (rawMax <= 0f) 1f else rawMax
    val maxEntryIndex = entries.indexOfFirst { it.value == rawMax }
    var selectedIndex by remember(entries) { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Column(
                modifier = Modifier.width(YAxisWidth).height(maxBarHeight),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf(axisMax, axisMax / 2f, 0f).forEach { value ->
                    Text(
                        value.roundToInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(YAxisSpacing))
            Row(
                modifier = Modifier.weight(1f).height(maxBarHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                entries.forEachIndexed { index, entry ->
                    val heightFraction = (entry.value / axisMax).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(maxBarHeight)
                            .clickable { selectedIndex = if (selectedIndex == index) null else index },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 3.dp)
                                .height(maxBarHeight * heightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    when {
                                        index == selectedIndex -> MaterialTheme.colorScheme.primary
                                        index == maxEntryIndex && rawMax > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                ),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.padding(start = YAxisWidth + YAxisSpacing, top = 8.dp)) {
            entries.forEach { entry ->
                Text(
                    entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        selectedIndex?.let { index ->
            val entry = entries[index]
            Text(
                "${entry.label} : ${String.format(Locale.getDefault(), "%.1f", entry.value)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = YAxisWidth + YAxisSpacing, top = 8.dp),
            )
        }
    }
}
