package com.example.wizardlydo.screens.tasks

import android.Manifest
import android.os.Build
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
import androidx.compose.runtime.getValue
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
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.getDaysRemaining
import com.example.wizardlydo.screens.tasks.comps.CharacterStatsSection
import com.example.wizardlydo.screens.tasks.comps.EmptyTaskList
import com.example.wizardlydo.screens.tasks.comps.ErrorMessage
import com.example.wizardlydo.screens.tasks.comps.FullScreenLoading
import com.example.wizardlydo.screens.tasks.comps.TaskBottomBar
import com.example.wizardlydo.screens.tasks.comps.TaskFilterChips
import com.example.wizardlydo.screens.tasks.comps.TaskListSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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

    // Initialize notification service in ViewModel
    LaunchedEffect(taskNotificationService) {
        viewModel.setNotificationService(taskNotificationService)
    }

    // Track notification badge state
    var showNotificationBadge by remember { mutableStateOf(false) }

    // Request notification permission
    val notificationPermission = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        if (!notificationPermission.status.isGranted) {
            notificationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        // Check for upcoming/due tasks to determine if badge should be shown
        coroutineScope.launch {
            val tasks = viewModel.getUpcomingTasksSync()
            showNotificationBadge = tasks.isNotEmpty()
        }
    }

    LaunchedEffect(state.recentlyCreatedTask) {
        state.recentlyCreatedTask?.let { newTask ->
            // Show creation notification with expandable style
            taskNotificationService.showTaskCreatedNotification(newTask)

            // Also schedule future reminder notifications
            if (newTask.dueDate != null && !newTask.isCompleted) {
                taskNotificationService.scheduleTaskNotification(newTask)
            }

            // Show snackbar for the newly created task
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
                    // Reset the state after showing the snackbar
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
                    // Notification bell with optional badge
                    Box {
                        IconButton(
                            onClick = {
                                // Show all upcoming tasks as a notification group
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
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notifications"
                            )
                        }

                        // Show red notification badge if there are upcoming tasks
                        if (showNotificationBadge) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
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
        // Use TaskContent for the main content area
        TaskContent(
            modifier = Modifier.padding(padding),
            state = state,
            onCompleteTask = { taskId ->
                viewModel.completeTask(taskId, taskNotificationService)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Task completed! +XP earned",
                        duration = SnackbarDuration.Short
                    )
                }
                // Cancel any notifications for this task since it's completed
                taskNotificationService.cancelTaskNotification(taskId)
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
                    // Cancel any notifications for this task since it's deleted
                    taskNotificationService.cancelTaskNotification(taskId)
                }
            },
            onDamageTaken = { damage, currentHealth ->
                // Handle damage taken events if needed
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lost $damage HP! Current HP: $currentHealth",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            taskNotificationService = taskNotificationService,
            viewModel = viewModel
        )
    }
}

@Composable
fun TaskContent(
    modifier: Modifier = Modifier,
    state: TaskUiState,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onDamageTaken: (damage: Int, currentHealth: Int) -> Unit = { _, _ -> },
    taskNotificationService: TaskNotificationService? = null,
    viewModel: TaskViewModel? = null
) {
    val wizardProfile = state.wizardProfile?.getOrNull()

    // Calculate tasks needed for level progression
    val tasksToNextLevel = wizardProfile?.let { viewModel?.getTasksToNextLevel(it) } ?: 0
    val totalTasksForLevel = wizardProfile?.level?.let { viewModel?.getTotalTasksForLevel(it) } ?: 0

    Column(modifier.fillMaxSize()) {
        // Character stats section
        CharacterStatsSection(
            wizardResult = state.wizardProfile,
            modifier = Modifier.padding(vertical = 4.dp), // Reduced vertical padding
            tasksToNextLevel = tasksToNextLevel,
            totalTasksForLevel = totalTasksForLevel
        )

        Spacer(Modifier.height(8.dp)) // Reduced spacing

        // Filter chips
        TaskFilterChips(
            currentFilter = state.currentFilter,
            onFilterChange = { filter ->
                state.onFilterChange?.invoke(filter)
            }
        )

        Spacer(Modifier.height(4.dp)) // Reduced spacing

        // Wrap task list in a weight modifier to ensure it takes remaining space
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.error != null -> ErrorMessage(error = state.error)
                state.isLoading -> FullScreenLoading()
                state.filteredTasks.isEmpty() -> EmptyTaskList()
                else -> TaskListSection(
                    tasks = state.filteredTasks,
                    onCompleteTask = { taskId ->
                        onCompleteTask(taskId)

                        // Find the task and wizard to send notification
                        val completedTask = state.filteredTasks.find { it.id == taskId }
                        completedTask?.let { task ->
                            wizardProfile?.let { profile ->
                                // Calculate HP gain based on priority and level
                                val hpGain = when (task.priority) {
                                    Priority.HIGH -> if(profile.level < 5) 15 else if(profile.level < 8) 12 else 7
                                    Priority.MEDIUM -> if(profile.level < 5) 10 else if(profile.level < 8) 8 else 5
                                    Priority.LOW -> if(profile.level < 5) 5 else if(profile.level < 8) 4 else 2
                                }

                                // Show completion notification if service is available
                                taskNotificationService?.showTaskCompletionNotification(
                                    task = task,
                                    wizardProfile = profile,
                                    hpGained = hpGain
                                )
                            }
                        }
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
    WizardlyDoTheme {
        TaskContent(
            state = TaskUiState(
                isLoading = false,
                wizardProfile = Result.success(
                    WizardProfile(
                        level = 5,
                        experience = 250
                    )
                ),
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
                currentFilter = TaskFilter.ALL
            ),
            onCompleteTask = {},
            onEditTask = {},
            onDeleteTask = {},
            onDamageTaken = { _, _ -> }
        )
    }
}