package com.example.wizardlydo.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.tasks.Task

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TaskItem(
    taskEntity: Task, onComplete: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    // State for completion confirmation dialog
    var showCompletionDialog by remember { mutableStateOf(false) }
    val daysRemaining = taskEntity.getDaysRemaining()

    // Completion confirmation dialog
    if (showCompletionDialog) {
        AlertDialog(onDismissRequest = { showCompletionDialog = false },
            title = { Text("Complete Task") },
            text = {
                Column {
                    Text("Mark this task as completed?")
                    daysRemaining?.let { days ->
                        Text(
                            text = if (days > 0) "Due in $days day${if (days > 1) "s" else ""}"
                            else "This task is overdue!", color = when {
                                days <= 0 -> MaterialTheme.colorScheme.error
                                days <= 3 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Completing tasks gives you XP and rewards!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onComplete()
                    showCompletionDialog = false
                }) {
                    Text("Complete", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) {
                    Text("Cancel")
                }
            })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = taskEntity.isCompleted, onCheckedChange = { _ ->
                if (!taskEntity.isCompleted) {
                    showCompletionDialog = true
                }
            })

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = taskEntity.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (taskEntity.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )

                    // Days remaining badge
                    daysRemaining?.takeIf { it >= 0 }?.let { days ->
                        Surface(
                            shape = CircleShape,
                            color = getDaysRemainingColor(days).copy(alpha = 0.2f),
                            contentColor = getDaysRemainingColor(days),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$days",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (taskEntity.description.isNotBlank()) {
                    Text(
                        text = taskEntity.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    taskEntity.dueDate?.let {
                        Text(
                            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (taskEntity.category != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = taskEntity.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            PriorityIndicator(priority = taskEntity.priority)
        }
    }
}



@Composable
private fun getDaysRemainingColor(days: Int): Color {
    return when {
        days <= 0 -> MaterialTheme.colorScheme.error // Overdue
        days <= 1 -> MaterialTheme.colorScheme.error // Due today/tomorrow
        days <= 3 -> MaterialTheme.colorScheme.tertiary // Due in 2-3 days
        else -> MaterialTheme.colorScheme.primary // Due later
    }
}

fun Task.getDaysRemaining(): Int? {
    return dueDate?.let { dueDateMillis ->
        val currentTime = System.currentTimeMillis()
        val diff = dueDateMillis - currentTime
        TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1 // +1 to count current partial day
    }
}