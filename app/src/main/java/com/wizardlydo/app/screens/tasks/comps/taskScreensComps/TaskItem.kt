package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    taskEntity: Task,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isEditing: Boolean = false
) {
    var showCompletionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe left to right - Complete task
                    if (!taskEntity.isCompleted) {
                        showCompletionDialog = true
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe right to left - Delete task
                    showDeleteDialog = true
                    false // Don't dismiss
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = {
                showCompletionDialog = false
            },
            title = { Text("Complete Task") },
            text = {
                Column {
                    Text("Mark this task as completed?")
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
                TextButton(onClick = {
                    showCompletionDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text("Delete Task") },
            text = {
                Column {
                    Text("Are you sure you want to delete this task?")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "\"${taskEntity.title}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset swipe state when dialogs are dismissed
    LaunchedEffect(showCompletionDialog, showDeleteDialog) {
        if (!showCompletionDialog && !showDeleteDialog) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(
                dismissDirection = dismissState.dismissDirection,
                isCompleted = taskEntity.isCompleted
            )
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEdit)
                    .then(
                        if (isEditing) {
                            Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Priority border on the left
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(80.dp)
                            .background(
                                when (taskEntity.priority) {
                                    Priority.LOW -> Color.Green
                                    Priority.MEDIUM -> Color(0xFFFFA500) // Orange
                                    Priority.HIGH -> Color.Red
                                }
                            )
                    )

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = taskEntity.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (taskEntity.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
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
                }
            }
        }
    )
}

@Composable
private fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    isCompleted: Boolean
) {
    val color = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            // Left to right swipe - Complete (only if not already completed)
            if (isCompleted) Color.Gray else Color(0xFF4CAF50)
        }
        SwipeToDismissBoxValue.EndToStart -> {
            // Right to left swipe - Delete
            Color(0xFFFF5252) // Red
        }
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    val icon = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            if (isCompleted) Icons.Default.Done else Icons.Default.Check
        }
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        SwipeToDismissBoxValue.Settled -> Icons.Default.Delete
    }

    val alignment = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = when (dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> "Complete"
                SwipeToDismissBoxValue.EndToStart -> "Delete"
                SwipeToDismissBoxValue.Settled -> ""
            },
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}