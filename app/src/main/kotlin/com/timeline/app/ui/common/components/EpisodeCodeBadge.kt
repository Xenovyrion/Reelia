package com.timeline.app.ui.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.app.ui.theme.JetBrainsMonoFontFamily

/**
 * Monospace `S03E07`-style episode code, tinted at ~12% opacity of its status color —
 * used in Detail's episode list and Home's continue-watching cards.
 */
@Composable
fun EpisodeCodeBadge(
    seasonNumber: Int,
    episodeNumber: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier,
    ) {
        Text(
            text = "S%02dE%02d".format(seasonNumber, episodeNumber),
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp,
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
