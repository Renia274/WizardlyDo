package com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val expPerLevel = 1000
    val nextLevel = level + 1
    val expProgress = experience.toFloat() / expPerLevel.toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
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
                text = "$tasksCompleted/$totalTasksForLevel tasks",
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { expProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Level $level → $nextLevel",
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

