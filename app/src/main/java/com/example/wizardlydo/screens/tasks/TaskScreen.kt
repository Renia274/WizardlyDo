package com.example.wizardlydo.screens.tasks

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.wizardlydo.screens.tasks.comps.TaskSearchBar
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
    onSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val taskNotificationService = remember { TaskNotificationService(context) }

    // Search state
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }

    // Get current wizard profile and guarantee it's not null for UI updates
    val wizardProfile = state.wizardProfile?.getOrNull()

    // These state values will be updated directly from the wizard profile
    val currentHealth = wizardProfile?.health ?: 100
    val currentMaxHealth = wizardProfile?.maxHealth ?: 100
    val currentStamina = wizardProfile?.stamina ?: 50
    val currentLevel = wizardProfile?.level ?: 1
    val currentExp = wizardProfile?.experience ?: 0

    // Track FAB and BottomBar visibility
    var isFabVisible by remember { mutableStateOf(true) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    // Calculate task progression
    val (completedTasks, totalTasksForLevel) = remember(wizardProfile) {
        derivedStateOf {
            wizardProfile?.let { profile ->
                val totalNeeded = when {
                    profile.level < 5 -> 10  // Increased from 4 to 10 for slower progression
                    profile.level < 8 -> 15  // Increased from 6 to 15 for slower progression
                    else -> 20  // Increased from 10 to 20 for slower progression
                }
                val remaining = viewModel.getTasksToNextLevel(profile)
                Pair((totalNeeded - remaining).coerceAtLeast(0), totalNeeded)
            } ?: Pair(0, 10)  // Default to 10 tasks for level 1
        }
    }.value

    // Ensure we load data when the screen starts
    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.setNotificationService(taskNotificationService)
    }

    // Apply search filters when they change
    LaunchedEffect(searchQuery, selectedPriority, state.currentFilter) {
        if (isSearchVisible) {
            viewModel.applySearchFilters(
                query = searchQuery,
                priority = selectedPriority,
                type = state.currentFilter
            )
        }
    }

    // Track search visibility
    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            viewModel.activateSearch()
        } else {
            viewModel.deactivateSearch()
        }
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
            if (isSearchVisible) {
                // Show search bar when search is active
                TaskSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCloseSearch = {
                        isSearchVisible = false
                        searchQuery = ""
                        selectedPriority = null
                        viewModel.deactivateSearch()
                    },
                    selectedFilter = state.currentFilter,
                    onFilterChange = { viewModel.setFilter(it) },
                    selectedPriority = selectedPriority,
                    onPriorityChange = { selectedPriority = it },
                    viewModel = viewModel
                )
            } else {
                // Show regular top app bar when search is not active
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
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                TaskBottomBar(
                    onHome = onHome,
                    onSearch = {
                        isSearchVisible = true
                        viewModel.activateSearch()
                    },
                    onSettings = onSettings
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCreateTask,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, "Create Task")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Task")
                }
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
            onNextPage = { viewModel.nextPage() },
            onPreviousPage = { viewModel.previousPage() },
            onDamageTaken = { damage, currentHealth ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lost $damage HP! Current HP: $currentHealth",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onNavigationBarVisibilityChange = { visible ->
                isFabVisible = visible
                isBottomBarVisible = visible
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
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onNextPage: () -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onDamageTaken: (damage: Int, currentHealth: Int) -> Unit = { _, _ -> },
    onNavigationBarVisibilityChange: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Responsive values
    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)
    val verticalSpacing = (screenHeight * 0.01f).coerceIn(4.dp, 8.dp)

    // Debug log to verify proper content values
    LaunchedEffect(wizardProfile, health, stamina, experience) {
        Log.d("TaskContent", "Content values - " +
                "Health: $health/$maxHealth, " +
                "Stamina: $stamina, " +
                "XP: $experience, " +
                "Tasks: $tasksCompleted/$totalTasksForLevel, " +
                "Current page: ${state.currentPage}/${state.totalPages}, " +
                "Wizard Profile null? ${wizardProfile == null}")
    }

    Column(modifier.fillMaxSize()) {
        CharacterStatsSection(
            wizardResult = state.wizardProfile,
            modifier = Modifier.padding(vertical = verticalSpacing),
            health = health,
            maxHealth = maxHealth,
            stamina = stamina,
            experience = experience,
            tasksCompleted = tasksCompleted,
            totalTasksForLevel = totalTasksForLevel
        )

        wizardProfile?.let { LevelUpIndicator(it.level) }

        Spacer(Modifier.height(verticalSpacing))

        TaskFilterChips(
            currentFilter = state.currentFilter,
            onFilterChange = { filter ->
                state.onFilterChange?.invoke(filter)
            }
        )

        Spacer(Modifier.height(verticalSpacing / 2))

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.error != null -> ErrorMessage(error = state.error)
                state.isLoading -> FullScreenLoading()
                state.filteredTasks.isEmpty() -> EmptyTaskList()
                else -> TaskListSection(
                    tasks = state.filteredTasks,
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    onNextPage = onNextPage,
                    onPreviousPage = onPreviousPage,
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
                    },
                    onNavigationBarVisibilityChange = onNavigationBarVisibilityChange
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
        consecutiveTasksCompleted = 0,
        wizardClass = WizardClass.MYSTWEAVER
    )

    val sampleTasks = (1..15).map { index ->
        Task(
            id = index,
            title = "Task $index",
            description = "Description for task $index",
            dueDate = if (index % 3 == 0) null else System.currentTimeMillis() + (index * 24 * 60 * 60 * 1000),
            priority = when (index % 3) {
                0 -> Priority.LOW
                1 -> Priority.MEDIUM
                else -> Priority.HIGH
            },
            userId = "",
            isCompleted = index % 4 == 0, // Every 4th task is completed
            createdAt = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000),
            isDaily = index % 5 == 0, // Every 5th task is daily
            category = when (index % 4) {
                0 -> "Work"
                1 -> "Personal"
                2 -> "School"
                else -> "Chores"
            }
        )
    }

    val paginatedTasks = sampleTasks.take(5)

    WizardlyDoTheme {
        TaskContent(
            state = TaskUiState(
                isLoading = false,
                wizardProfile = Result.success(wizardProfile),
                tasks = sampleTasks,
                filteredTasks = paginatedTasks,
                currentFilter = TaskFilter.ALL,
                onFilterChange = {},
                currentPage = 1,
                totalPages = 3
            ),
            wizardProfile = wizardProfile,
            tasksCompleted = 2,
            totalTasksForLevel = 10,
            onCompleteTask = {},
            onEditTask = {},
            onDeleteTask = {},
            onNextPage = {},

            onDamageTaken = { _, _ -> },
            modifier = Modifier,
            health = 100,
            maxHealth = 150,
            stamina = 75,
            experience = 250,
            onPreviousPage = {},
            onNavigationBarVisibilityChange = { }
        )
    }
}