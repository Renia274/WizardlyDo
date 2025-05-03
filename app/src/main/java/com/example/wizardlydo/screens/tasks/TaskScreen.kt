package com.example.wizardlydo.screens.tasks

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.getDaysRemaining
import com.example.wizardlydo.screens.tasks.comps.CharacterStatsSection
import com.example.wizardlydo.screens.tasks.comps.EmptyTaskList
import com.example.wizardlydo.screens.tasks.comps.ErrorMessage
import com.example.wizardlydo.screens.tasks.comps.FullScreenLoading
import com.example.wizardlydo.screens.tasks.comps.LevelUpIndicator
import com.example.wizardlydo.screens.tasks.comps.TaskBottomBar
import com.example.wizardlydo.screens.tasks.comps.TaskFilterChips
import com.example.wizardlydo.screens.tasks.comps.TaskListSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel(),
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onEditMode: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val taskNotificationService = remember { TaskNotificationService(context) }

    // Get current wizard profile and guarantee it's not null for UI updates
    val wizardProfile = state.wizardProfile?.getOrNull()

    // These state values will be updated directly from the wizard profile
    // Critical: Use mutableStateOf instead of remember + mutableIntStateOf
    // to ensure they're updated when collected from state flow
    val currentHealth = wizardProfile?.health ?: 100
    val currentMaxHealth = wizardProfile?.maxHealth ?: 100
    val currentStamina = wizardProfile?.stamina ?: 50
    val currentLevel = wizardProfile?.level ?: 1
    val currentExp = wizardProfile?.experience ?: 0
    val totalTasksCompleted = wizardProfile?.totalTasksCompleted ?: 0

    // Log current profile state for debugging
    LaunchedEffect(wizardProfile) {
        wizardProfile?.let {
            Log.d("TaskScreen", "Profile updated: Level=${it.level}, " +
                    "HP=${it.health}/${it.maxHealth}, " +
                    "Stamina=${it.stamina}, " +
                    "XP=${it.experience}, " +
                    "Tasks completed=${it.totalTasksCompleted}")
        }
    }

    // Calculate task progression
    val (completedTasks, totalTasksForLevel) = remember(wizardProfile) {
        derivedStateOf {
            wizardProfile?.let { profile ->
                val totalNeeded = when {
                    profile.level < 5 -> 4
                    profile.level < 8 -> 6
                    else -> 10
                }
                val remaining = viewModel.getTasksToNextLevel(profile)
                Pair((totalNeeded - remaining).coerceAtLeast(0), totalNeeded)
            } ?: Pair(0, 4)
        }
    }.value

    // Ensure we load data when the screen starts
    LaunchedEffect(Unit) {
        Log.d("TaskScreen", "Initial data load")
        viewModel.loadData()
        viewModel.setNotificationService(taskNotificationService)
    }

    // Notification permission and badge state
    val notificationPermission = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )
    var showNotificationBadge by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!notificationPermission.status.isGranted) {
            notificationPermission.launchPermissionRequest()
        }
        coroutineScope.launch {
            val tasks = viewModel.getUpcomingTasksSync()
            showNotificationBadge = tasks.isNotEmpty()
        }
    }

    LaunchedEffect(state.recentlyCreatedTask) {
        state.recentlyCreatedTask?.let { newTask ->
            taskNotificationService.showTaskCreatedNotification(newTask)
            if (newTask.dueDate != null && !newTask.isCompleted) {
                taskNotificationService.scheduleTaskNotification(newTask)
            }

            newTask.getDaysRemaining()?.let { days ->
                val message = if (days in 1..7) {
                    "Added: ${newTask.title} (Due in $days day${if (days != 1) "s" else ""})"
                } else {
                    "Added: ${newTask.title}"
                }

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.resetRecentlyCreatedTask()
                }
            } ?: run {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Added: ${newTask.title}",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.resetRecentlyCreatedTask()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val tasks = viewModel.getUpcomingTasksSync()
                                    if (tasks.isNotEmpty()) {
                                        taskNotificationService.showTaskSummaryNotification(tasks)
                                        showNotificationBadge = false
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = "No upcoming tasks found",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        if (showNotificationBadge) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopStart)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            TaskBottomBar(
                onHome = onHome,
                onEditMode = onEditMode,
                onSettings = onSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, "Create Task")
            }
        }
    ) { padding ->
        TaskContent(
            modifier = Modifier.padding(padding),
            state = state,
            wizardProfile = wizardProfile,
            health = currentHealth,
            maxHealth = currentMaxHealth,
            stamina = currentStamina,
            experience = currentExp,
            tasksCompleted = completedTasks,
            totalTasksForLevel = totalTasksForLevel,
            totalTasksCompletedCount = totalTasksCompleted,
            taskStreakCount = 0, // Set to 0 to remove streak functionality as requested
            onCompleteTask = { taskId ->
                Log.d("TaskScreen", "Complete task clicked: $taskId")
                coroutineScope.launch {
                    viewModel.completeTask(taskId, taskNotificationService)
                }
            },
            onEditTask = onEditTask,
            onDeleteTask = { taskId ->
                viewModel.deleteTask(taskId) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Task deleted",
                            duration = SnackbarDuration.Short
                        )
                    }
                    taskNotificationService.cancelTaskNotification(taskId)
                }
            },
            onDamageTaken = { damage, currentHealth ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lost $damage HP! Current HP: $currentHealth",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
}


@Composable
fun TaskContent(
    modifier: Modifier = Modifier,
    state: TaskUiState,
    wizardProfile: WizardProfile?,
    health: Int,
    maxHealth: Int,
    stamina: Int,
    experience: Int,
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    totalTasksCompletedCount: Int,
    taskStreakCount: Int = 0,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onDamageTaken: (damage: Int, currentHealth: Int) -> Unit = { _, _ -> }
) {
    // Debug log to verify proper content values
    LaunchedEffect(wizardProfile, health, stamina, experience) {
        Log.d("TaskContent", "Content values - " +
                "Health: $health/$maxHealth, " +
                "Stamina: $stamina, " +
                "XP: $experience, " +
                "Tasks: $tasksCompleted/$totalTasksForLevel, " +
                "Total Completed: $totalTasksCompletedCount, " +
                "Wizard Profile null? ${wizardProfile == null}")
    }

    Column(modifier.fillMaxSize()) {
        // Pass the direct values to CharacterStatsSection
        // If wizardProfile is null, we still want to show stats using the direct values
        CharacterStatsSection(
            wizardResult = state.wizardProfile,  // Use the original state.wizardProfile directly
            modifier = Modifier.padding(vertical = 4.dp),
            health = health,
            maxHealth = maxHealth,
            stamina = stamina,
            experience = experience,
            tasksCompleted = tasksCompleted,
            totalTasksForLevel = totalTasksForLevel,
            totalTasksCompletedCount = totalTasksCompletedCount,
            taskStreakCount = taskStreakCount
        )

        // Show level up indicator
        wizardProfile?.let { LevelUpIndicator(it.level) }

        Spacer(Modifier.height(8.dp))

        TaskFilterChips(
            currentFilter = state.currentFilter,
            onFilterChange = { filter ->
                state.onFilterChange?.invoke(filter)
            }
        )

        Spacer(Modifier.height(4.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.error != null -> ErrorMessage(error = state.error)
                state.isLoading -> FullScreenLoading()
                state.filteredTasks.isEmpty() -> EmptyTaskList()
                else -> TaskListSection(
                    tasks = state.filteredTasks,
                    onCompleteTask = { taskId ->
                        Log.d("TaskContent", "Completing task: $taskId")
                        onCompleteTask(taskId)
                    },
                    onEditTask = onEditTask,
                    onDeleteTask = { taskId ->
                        onDeleteTask(taskId)
                        val deletedTask = state.filteredTasks.find { it.id == taskId }
                        deletedTask?.let { task ->
                            val damage = when (task.priority) {
                                Priority.HIGH -> 20
                                Priority.MEDIUM -> 10
                                Priority.LOW -> 5
                            }
                            val currentHealth = wizardProfile?.health ?: 100
                            onDamageTaken(damage, currentHealth - damage)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskContentPreview() {
    val wizardProfile = WizardProfile(
        wizardName = "Taltooooonnn",
        level = 5,
        experience = 250,
        health = 100,
        maxHealth = 100,
        stamina = 75,
        totalTasksCompleted = 15,
        consecutiveTasksCompleted = 3,
        wizardClass = WizardClass.MYSTWEAVER
    )

    WizardlyDoTheme {
        TaskContent(
            state = TaskUiState(
                isLoading = false,
                wizardProfile = Result.success(wizardProfile),
                filteredTasks = listOf(
                    Task(
                        id = 1,
                        title = "Complete homework",
                        description = "Math and science homework",
                        dueDate = System.currentTimeMillis(),
                        priority = Priority.HIGH,
                        userId = "",
                        isCompleted = false,
                        createdAt = System.currentTimeMillis() - 86400000,
                        isDaily = false,
                        category = "School"
                    ),
                    Task(
                        id = 2,
                        title = "Go shopping",
                        description = "Buy groceries",
                        dueDate = System.currentTimeMillis() + 86400000,
                        priority = Priority.MEDIUM,
                        userId = "",
                        isCompleted = true,
                        createdAt = System.currentTimeMillis() - 172800000,
                        isDaily = true,
                        category = "Chores"
                    )
                ),
                currentFilter = TaskFilter.ALL,
                onFilterChange = {}
            ),
            wizardProfile = wizardProfile,
            tasksCompleted = 2,
            totalTasksForLevel = 6,
            totalTasksCompletedCount = 15,
            taskStreakCount = 3,
            onCompleteTask = {},
            onEditTask = {},
            onDeleteTask = {},
            onDamageTaken = { _, _ -> },
            modifier = Modifier,
            health = 100,
            maxHealth = 150,
            stamina = 75,
            experience = 250
        )
    }
}