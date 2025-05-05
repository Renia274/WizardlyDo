package com.example.wizardlydo.screens.tasks.comps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.comps.getHairResourceId
import com.example.wizardlydo.comps.getOutfitResourceId
import com.example.wizardlydo.comps.getSkinResourceId
import com.example.wizardlydo.comps.items.EquippedItems
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@Composable
fun WizardAvatar(
    wizardResult: Result<WizardProfile?>?,
    equippedItems: EquippedItems?,
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Background layer - only show if equipped
                    equippedItems?.background?.let { background ->
                        Image(
                            painter = painterResource(id = background.resourceId),
                            contentDescription = "Background",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            alpha = 0.7f
                        )
                    }

                    // Skin/Body
                    Image(
                        painter = painterResource(id = getSkinResourceId(wizardProfile.skinColor)),
                        contentDescription = "Character Body",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .offset(x = (-10).dp, y = (-6).dp)
                    )

                    // Outfit (equipped from inventory or default)
                    if (equippedItems?.outfit != null) {
                        Image(
                            painter = painterResource(id = equippedItems.outfit.resourceId),
                            contentDescription = "Character Outfit",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.Center)
                                .offset(x = (-10).dp, y = (-6).dp)
                        )
                    } else {
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
                                .offset(x = (-10).dp, y = (-6).dp)
                        )
                    }

                    // Hair - Convert String to Int for hairStyle
                    Image(
                        painter = painterResource(
                            id = getHairResourceId(
                                wizardProfile.gender,
                                wizardProfile.hairStyle.toIntOrNull() ?: 0,
                                wizardProfile.hairColor
                            )
                        ),
                        contentDescription = "Character Hair",
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 10.dp)
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
    modifier: Modifier = Modifier,
    health: Int,
    maxHealth: Int,
    stamina: Int,
    maxStamina: Int,
    experience: Int,
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    equippedItems: EquippedItems? = null
) {
    val wizardProfile = wizardResult?.getOrNull()

    // Animate values
    val animatedHealth by animateIntAsState(
        targetValue = health,
        animationSpec = tween(durationMillis = 500),
        label = "health"
    )

    val animatedStamina by animateIntAsState(
        targetValue = stamina,
        animationSpec = tween(durationMillis = 500),
        label = "stamina"
    )

    val animatedExp by animateIntAsState(
        targetValue = experience,
        animationSpec = tween(durationMillis = 500),
        label = "experience"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            // Background layer - only show if equipped
            equippedItems?.background?.let { bg ->
                Image(
                    painter = painterResource(id = bg.resourceId),
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            // Content layer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Character info section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WizardAvatar(
                        wizardResult = wizardResult,
                        equippedItems = equippedItems,
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    wizardProfile?.let { wizard ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wizard.wizardName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = wizard.wizardClass.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Level ${wizard.level}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StatBar(
                    label = "HP",
                    value = animatedHealth,
                    maxValue = maxHealth,
                    color = Color(0xFFE53935),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatBar(
                    label = "Stamina",
                    value = animatedStamina,
                    maxValue = maxStamina,
                    color = Color(0xFF43A047),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // XP and tasks to level up
                CompactLevelProgressSection(
                    level = wizardProfile?.level ?: 1,
                    experience = animatedExp,
                    tasksCompleted = tasksCompleted,
                    totalTasksForLevel = totalTasksForLevel
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Task progress section
                TaskProgressSection(
                    tasksCompleted = tasksCompleted,
                    totalTasksForLevel = totalTasksForLevel
                )
            }
        }
    }
}

@Composable
private fun StatBar(
    label: String, value: Int, maxValue: Int, color: Color, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
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

        Spacer(modifier = Modifier.height(2.dp))

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
fun CompactLevelProgressSection(
    level: Int, experience: Int, tasksCompleted: Int, totalTasksForLevel: Int
) {
    val expPerLevel = 1000

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // XP display
            Text(
                text = "$experience/$expPerLevel XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Task progress display - use the passed direct value
            Text(
                text = "$tasksCompleted of $totalTasksForLevel tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // XP progress bar
        LinearProgressIndicator(
            progress = { experience.toFloat() / expPerLevel.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 2.dp),
            color = Color(0xFFFFB300),
            trackColor = Color(0xFFFFB300).copy(alpha = 0.2f)
        )
    }
}


@Composable
fun TaskProgressSection(
    tasksCompleted: Int,
    totalTasksForLevel: Int,

    ) {
    val taskInfoColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header row - removed completion counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Progression",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Task progression display - use direct values
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$tasksCompleted/$totalTasksForLevel completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Segmented progress bar with direct values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (i in 0 until totalTasksForLevel) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i < tasksCompleted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            // HP cap info
            Text(
                text = "HP Cap: 150",
                style = MaterialTheme.typography.labelSmall,
                color = taskInfoColor,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun TaskFilterChips(
    currentFilter: TaskFilter, onFilterChange: (TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.name) })
        }
    }
}


@Composable
fun TaskListSection(
    tasks: List<Task>,
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onNavigationBarVisibilityChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = 0,
        initialFirstVisibleItemScrollOffset = 0
    )

    // Track loading state for pagination
    var isLoadingPage by remember { mutableStateOf(false) }

    // Detect scroll position to show/hide FAB and BottomBar
    val navigationVisibilityState = remember { mutableStateOf(true) }

    LaunchedEffect(lazyListState) {
        var previousPendingFirstIndex = 0
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { currentFirstIndex ->
            when {
                currentFirstIndex > previousPendingFirstIndex -> {
                    // Scrolling down - hide FAB and BottomBar
                    if (navigationVisibilityState.value) {
                        navigationVisibilityState.value = false
                        onNavigationBarVisibilityChange(false)
                    }
                }

                currentFirstIndex < previousPendingFirstIndex -> {
                    // Scrolling up - show FAB and BottomBar
                    if (!navigationVisibilityState.value) {
                        navigationVisibilityState.value = true
                        onNavigationBarVisibilityChange(true)
                    }
                }
            }
            previousPendingFirstIndex = currentFirstIndex
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
    ) {
        // Task list with swipe gestures for pagination
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                // Add swipe gestures
                userScrollEnabled = true
            ) {
                items(tasks) { taskEntity ->
                    TaskItem(
                        taskEntity = taskEntity,
                        onComplete = { onCompleteTask(taskEntity.id) },
                        onEdit = { onEditTask(taskEntity.id) },
                        onDelete = { onDeleteTask(taskEntity.id) }
                    )
                }

                // Space for loading indicator
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Auto-load next page when reaching bottom
            if (tasks.isNotEmpty()) {
                LaunchedEffect(lazyListState) {
                    snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .distinctUntilChanged()
                        .collect { lastVisibleIndex ->
                            if (lastVisibleIndex == tasks.size - 1 && !isLoadingPage && currentPage < totalPages) {
                                isLoadingPage = true
                                onNextPage()
                                delay(500) // Small delay for better UX
                                isLoadingPage = false
                            }
                        }
                }

                // Show loading indicator when fetching next page
                if (isLoadingPage && currentPage < totalPages) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }

        // Page controls with swipe indication - always visible even when navigation bars are hidden
        if (totalPages > 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous page button
                    IconButton(
                        onClick = {
                            if (currentPage > 1) {
                                coroutineScope.launch {
                                    onPreviousPage()
                                }
                            }
                        },
                        enabled = currentPage > 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous page",
                            tint = if (currentPage > 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Page indicator with swipe hint
                    PageIndicator(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        modifier = Modifier.weight(1f),
                        showSwipeHint = true
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < totalPages) {
                                coroutineScope.launch {
                                    onNextPage()
                                }
                            }
                        },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next page",
                            tint = if (currentPage < totalPages)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    showSwipeHint: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { page ->
                Box(
                    modifier = Modifier
                        .size(if (page == currentPage - 1) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (page == currentPage - 1) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                        .animateContentSize(
                            animationSpec = tween(300)
                        )
                )

                if (page < totalPages - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        // Current page text
        Text(
            text = "Page $currentPage of $totalPages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Swipe hint (only show if enabled)
        if (showSwipeHint) {
            Text(
                text = "Scroll to auto-load next",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

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
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onInventory: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }

            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            IconButton(onClick = onInventory) {
                Icon(Icons.Default.Person, contentDescription = "Inventory")
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
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LevelUpIndicator(level: Int) {
    var showLevelUp by remember { mutableStateOf(false) }
    var currentLevel by remember { mutableIntStateOf(level) }

    LaunchedEffect(level) {
        if (level > currentLevel) {
            showLevelUp = true
            currentLevel = level
            delay(2000) // Show for 2 seconds
            showLevelUp = false
        }
    }

    AnimatedVisibility(
        visible = showLevelUp,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = "Level Up!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ErrorMessage(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), contentAlignment = Alignment.Center
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
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tasks found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    selectedPriority: Priority?,
    onPriorityChange: (Priority?) -> Unit,
    viewModel: TaskViewModel, // Added ViewModel parameter
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(searchQuery) } // Local state for input field

    // Effect to update local input state when searchQuery changes
    LaunchedEffect(searchQuery) {
        inputText = searchQuery
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Search bar
        SearchBar(
            query = inputText, // Use local state for input
            onQueryChange = { newText ->
                inputText = newText
                onSearchQueryChange(newText)
            },
            onSearch = {
                // Apply search on submit
                viewModel.activateSearch()
                expanded = false
            },
            active = expanded,
            onActiveChange = { isActive ->
                expanded = isActive
                // When closing without search button, apply filters anyway
                if (!isActive && inputText.isNotEmpty()) {
                    viewModel.activateSearch()
                }
            },
            placeholder = { Text("Search tasks...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    // Reset input and deactivate search
                    inputText = ""
                    onSearchQueryChange("")
                    viewModel.deactivateSearch()
                    onCloseSearch()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Filter options inside the expanded search bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {


                // Priority filter
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Any" option
                    FilterChip(
                        selected = selectedPriority == null,
                        onClick = { onPriorityChange(null) },
                        label = { Text("Any") }
                    )

                    // Priority options
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when (priority) {
                                                Priority.LOW -> Color.Green
                                                Priority.MEDIUM -> Color.Yellow
                                                Priority.HIGH -> Color.Red
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task filter
                Text(
                    text = "Task Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.name) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.activateSearch()
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
