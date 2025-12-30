package com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel

@Composable
fun TaskProgressSection(
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    wizardProfile: WizardProfile? = null,
    taskViewModel: TaskViewModel? = null
) {
    val taskProgress = if (totalTasksForLevel > 0) {
        tasksCompleted.toFloat() / totalTasksForLevel.toFloat()
    } else {
        0f
    }

    // Determine level range
    val levelRange = wizardProfile?.level?.let { level ->
        when (level) {
            in 1..4 -> "1-4"
            in 5..8 -> "5-8"
            in 9..14 -> "9-14"
            in 15..19 -> "15-19"
            in 20..24 -> "20-24"
            in 25..29 -> "25-29"
            else -> "30"
        }
    } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                wizardProfile?.let { profile ->
                    taskViewModel?.showTaskProgressToast(profile)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level Set $levelRange Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tap for details",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { taskProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = textColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$tasksCompleted/$totalTasksForLevel tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                wizardProfile?.let { profile ->
                    Text(
                        text = "${profile.experience}/1000 XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val remainingTasks = totalTasksForLevel - tasksCompleted
            Text(
                text = if (remainingTasks > 0) {
                    "Tap for details • Complete $remainingTasks more to advance"
                } else {
                    "Tap for details • Ready to advance!"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}