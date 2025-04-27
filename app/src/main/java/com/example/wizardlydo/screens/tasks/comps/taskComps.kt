package com.example.wizardlydo.screens.tasks.comps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.comps.getHairResourceId
import com.example.wizardlydo.comps.getOutfitResourceId
import com.example.wizardlydo.comps.getSkinResourceId
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@Composable
fun WizardAvatar(
    wizardResult: Result<WizardProfile?>?,
    modifier: Modifier = Modifier
) {
    val wizardProfile = wizardResult?.getOrNull()
    val error = wizardResult?.exceptionOrNull()

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when {
            wizardProfile != null -> {
                Box(modifier = Modifier.size(100.dp)) {
                    // Skin/Body
                    Image(
                        painter = painterResource(id = getSkinResourceId(wizardProfile.skinColor)),
                        contentDescription = "Character Body",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                    )

                    // Outfit
                    Image(
                        painter = painterResource(
                            id = getOutfitResourceId(
                                wizardProfile.wizardClass,
                                wizardProfile.outfit,
                                wizardProfile.gender
                            )
                        ),
                        contentDescription = "Character Outfit",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                    )

                    // Hair
                    Image(
                        painter = painterResource(
                            id = getHairResourceId(
                                wizardProfile.gender,
                                wizardProfile.hairStyle,
                                wizardProfile.hairColor
                            )
                        ),
                        contentDescription = "Character Hair",
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopCenter)
                            .offset(x = 10.dp, y = 24.dp)
                    )
                }
            }

            error != null -> {
                Text(
                    text = "⚠",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.size(40.dp)
                )

            }

            else -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CharacterStatsSection(
    wizardResult: Result<WizardProfile?>?,
    modifier: Modifier = Modifier
) {
    val wizardProfile = wizardResult?.getOrNull()
    val error = wizardResult?.exceptionOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            WizardAvatar(
                wizardResult = wizardResult,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name and Class
            when {
                error != null -> {
                    Text(
                        text = "Character Load Failed",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                wizardProfile != null -> {
                    Text(
                        text = wizardProfile.wizardName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = wizardProfile.wizardClass.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    Text(
                        text = "Loading Character...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Section
            if (wizardProfile != null) {
                StatBar(
                    label = "HP",
                    value = wizardProfile.health,
                    maxValue = wizardProfile.maxHealth,
                    color = Color(0xFFE53935),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatBar(
                    label = "Stamina",
                    value = wizardProfile.stamina,
                    maxValue = 100,
                    color = Color(0xFF43A047),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Level ${wizardProfile.level}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (error == null) {
                // Loading placeholders
                repeat(2) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value/$maxValue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = {
                if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun TaskFilterChips(
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.name) }
            )
        }
    }
}


@Composable
fun TaskListSection(
    tasks: List<Task>,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        items(tasks) { taskEntity ->
            TaskItem(
                taskEntity = taskEntity,
                onComplete = { onCompleteTask(taskEntity.id) },
                onEdit = { onEditTask(taskEntity.id) },
                onDelete = { onDeleteTask(taskEntity.id) }
            )
        }
    }
}


@Composable
fun TaskItem(
    taskEntity: Task,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // State for completion confirmation dialog
    var showCompletionDialog by remember { mutableStateOf(false) }
    val daysRemaining = taskEntity.getDaysRemaining()

    // Completion confirmation dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Complete Task") },
            text = {
                Column {
                    Text("Mark this task as completed?")
                    daysRemaining?.let { days ->
                        Text(
                            text = if (days > 0) "Due in $days day${if (days > 1) "s" else ""}"
                            else "This task is overdue!",
                            color = when {
                                days <= 0 -> MaterialTheme.colorScheme.error
                                days <= 3 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.Bold
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
                TextButton(
                    onClick = {
                        onComplete()
                        showCompletionDialog = false
                    }
                ) {
                    Text("Complete", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
            Checkbox(
                checked = taskEntity.isCompleted,
                onCheckedChange = { _ ->
                    if (!taskEntity.isCompleted) {
                        showCompletionDialog = true
                    }
                }
            )

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

// Add this extension function to your codebase
fun Task.getDaysRemaining(): Int? {
    return dueDate?.let { dueDateMillis ->
        val currentTime = System.currentTimeMillis()
        val diff = dueDateMillis - currentTime
        TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1 // +1 to count current partial day
    }
}


@Composable
fun PriorityIndicator(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> Color.Green
        Priority.MEDIUM -> Color.Yellow
        Priority.HIGH -> Color.Red
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
fun TaskBottomBar(
    onHome: () -> Unit,
    onEditMode: () -> Unit,
    onSettings: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = onEditMode) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun ErrorMessage(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error ?: "Unknown error occurred",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyTaskList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tasks found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}



