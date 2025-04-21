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
import androidx.compose.runtime.Composable
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


@Composable
fun WizardAvatar(
    wizardProfile: WizardProfile?,
    modifier: Modifier = Modifier
) {
    // If profile is null, show a placeholder avatar
    if (wizardProfile == null) {
        Box(
            modifier = modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            // Show a placeholder or loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    // Extract customization details from the profile
    val outfit = wizardProfile.outfit
    val hairStyle = wizardProfile.hairStyle
    val hairColor = wizardProfile.hairColor
    val gender = wizardProfile.gender
    val skinColor = wizardProfile.skinColor

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        // Get the skin resource based on skinColor
        val skinResourceId = getSkinResourceId(skinColor)

        // Skin/Body
        Image(
            painter = painterResource(id = skinResourceId),
            contentDescription = "Character Body",
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center)
        )


        val outfitResId = getOutfitResourceId(wizardProfile.wizardClass, outfit, gender)

        Image(
            painter = painterResource(id = outfitResId),
            contentDescription = "Character Outfit",
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center)
        )

        // Hair with proper styling
        val hairResId = getHairResourceId(gender, hairStyle, hairColor)

        Image(
            painter = painterResource(id = hairResId),
            contentDescription = "Character Hair",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopCenter)
                .offset(x = 10.dp, y = 35.dp)
        )
    }
}

@Composable
fun CharacterStatsSection(
    wizard: WizardProfile?,
    modifier: Modifier = Modifier
) {
    // Use wizard's properties directly
    val health = wizard?.health ?: 0
    val maxHealth = wizard?.maxHealth ?: 100
    val stamina = wizard?.stamina ?: 0
    val level = wizard?.level ?: 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WizardAvatar(
                wizardProfile = wizard,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = wizard?.wizardName ?: "Loading...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = wizard?.wizardClass?.name ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatBar(
                label = "HP",
                value = health,
                maxValue = maxHealth,
                color = Color(0xFFE53935),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatBar(
                label = "Stamina",
                value = stamina,
                maxValue = 100,
                color = Color(0xFF43A047),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Level $level",
                style = MaterialTheme.typography.bodyMedium
            )
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
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = "$value/$maxValue", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
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
    onEditTask: (Int) -> Unit
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
                onEdit = { onEditTask(taskEntity.id) }
            )
        }
    }
}

@Composable
fun TaskItem(
    taskEntity: Task,
    onComplete: () -> Unit,
    onEdit: () -> Unit
) {
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
                onCheckedChange = { _ -> onComplete() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = taskEntity.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (taskEntity.isCompleted) TextDecoration.LineThrough else null
                )

                if (taskEntity.description.isNotBlank()) {
                    Text(
                        text = taskEntity.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                taskEntity.dueDate?.let {
                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                        Date(it)
                    )
                    Text(
                        text = "Due: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (taskEntity.category != null) {
                    Surface(
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = { },
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = taskEntity.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            PriorityIndicator(priority = taskEntity.priority)
        }
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



