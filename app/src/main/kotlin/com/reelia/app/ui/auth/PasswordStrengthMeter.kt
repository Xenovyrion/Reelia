package com.reelia.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reelia.app.R
import com.reelia.app.ui.theme.StatusFavorite
import com.reelia.app.ui.theme.StatusPlanned
import com.reelia.app.ui.theme.StatusWatchingCompleted

private val FilledSegmentsByStrength = mapOf(
    PasswordStrength.TOO_SHORT to 0,
    PasswordStrength.WEAK to 1,
    PasswordStrength.MEDIUM to 2,
    PasswordStrength.STRONG to 3,
)

@Composable
private fun PasswordStrength.color(): Color = when (this) {
    PasswordStrength.TOO_SHORT, PasswordStrength.WEAK -> StatusFavorite
    PasswordStrength.MEDIUM -> StatusPlanned
    PasswordStrength.STRONG -> StatusWatchingCompleted
}

@Composable
private fun PasswordStrength.label(): String = stringResource(
    when (this) {
        PasswordStrength.TOO_SHORT -> R.string.password_strength_too_short
        PasswordStrength.WEAK -> R.string.password_strength_weak
        PasswordStrength.MEDIUM -> R.string.password_strength_medium
        PasswordStrength.STRONG -> R.string.password_strength_strong
    },
)

@Composable
fun PasswordStrengthMeter(password: String, modifier: Modifier = Modifier) {
    val strength = evaluatePasswordStrength(password)
    val filledSegments = FilledSegmentsByStrength.getValue(strength)
    val color = strength.color()

    Row(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < filledSegments) color else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                )
            }
        }
        Text(
            strength.label(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
