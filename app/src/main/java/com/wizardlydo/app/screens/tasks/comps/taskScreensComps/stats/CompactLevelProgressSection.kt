package com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CompactLevelProgressSection(
    level: Int,
    experience: Int,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val expPerLevel = 1000
    val progressToNextLevel = (experience.toFloat() / expPerLevel).coerceIn(0f, 1f)
    val nextLevel = level + 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$experience/$expPerLevel XP",
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )

        Text(
            text = "Level $level → $nextLevel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    LinearProgressIndicator(
        progress = { progressToNextLevel },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = textColor.copy(alpha = 0.2f)
    )
}
