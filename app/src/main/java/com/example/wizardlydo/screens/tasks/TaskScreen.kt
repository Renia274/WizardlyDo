package com.example.wizardlydo.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.comps.ErrorMessage
import com.example.wizardlydo.comps.FullScreenLoading
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.data.wizard.items.EquippedItems
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.EmptyTaskList
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.Level30CompletionDialog
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.LevelUpIndicator
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskBottomBar
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskFilterChips
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskListSection
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskSearchBar
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.CharacterStatsSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.inventory.InventoryViewModel
import com.example.wizardlydo.viewmodel.tasks.TaskViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel(),
    inventoryViewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onSettings: () -> Unit,
    onInventory: () -> Unit,
    onDonation: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val taskNotificationService = remember { TaskNotificationService(context) }

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }

    val equippedItems by inventoryViewModel.equippedItemsFlow.collectAsState()

    val wizardProfile = state.wizardProfile?.getOrNull()

    val currentHealth = wizardProfile?.health ?: 100
    val currentMaxHealth = wizardProfile?.maxHealth ?: WizardProfile.calculateMaxHealth(1)
    val currentStamina = wizardProfile?.stamina ?: 50
    val currentMaxStamina = wizardProfile?.maxStamina ?: WizardProfile.calculateMaxStamina(1)
    val currentLevel = wizardProfile?.level ?: 1
    val currentExp = wizardProfile?.experience ?: 0

    var isFabVisible by remember { mutableStateOf(true) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    val (completedTasksForLevel, totalTasksForLevel) = remember(wizardProfile) {
        wizardProfile?.let { profile ->
            val completed = viewModel.getTasksCompletedForLevel(profile)
            val total = viewModel.getTasksRequiredForLevel(profile.level)
            Pair(completed, total)
        } ?: Pair(0, 10)
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.setNotificationService(taskNotificationService)
    }

    LaunchedEffect(searchQuery, selectedPriority, state.currentFilter) {
        if (isSearchVisible) {
            viewModel.applySearchFilters(
                query = searchQuery,
                priority = selectedPriority,
                type = state.currentFilter
            )
        }
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            viewModel.activateSearch()
        } else {
            viewModel.deactivateSearch()
        }
    }

    // Show Level 30 Dialog
    if (state.showLevel30Dialog) {
        Level30CompletionDialog(
            onDismiss = { viewModel.hideLevel30Dialog() },
            onDonate = onDonation,
            wizardName = wizardProfile?.wizardName ?: "Wizard"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (isSearchVisible) {
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
                TopAppBar(
                    title = { Text("Task Manager") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                    onSettings = onSettings,
                    onInventory = onInventory,
                    onDonation = onDonation
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
            maxStamina = currentMaxStamina,
            experience = currentExp,
            tasksCompleted = completedTasksForLevel,
            totalTasksForLevel = totalTasksForLevel,
            equippedItems = equippedItems,
            onCompleteTask = { taskId ->
                coroutineScope.launch {
                    // Check if the wizard is defeated
                    if (viewModel.isWizardDefeated()) {
                        // Use the revival progress function
                        viewModel.updateRevivalProgress(taskId) { restoredHealth ->
                            // This callback is called when revival succeeds
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Your wizard has been revived with $restoredHealth HP!",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    } else {
                        // Normal completion when wizard is alive
                        viewModel.completeTask(taskId, taskNotificationService)
                    }
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
    maxStamina: Int,
    experience: Int,
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    equippedItems: EquippedItems? = null,
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

    // Calculate responsive horizontal padding
    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)
    val verticalSpacing = (screenHeight * 0.01f).coerceIn(4.dp, 8.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding) // Apply horizontal padding to the main column
    ) {
        CharacterStatsSection(
            wizardResult = state.wizardProfile,
            modifier = Modifier.padding(vertical = verticalSpacing),
            health = health,
            maxHealth = maxHealth,
            stamina = stamina,
            maxStamina = maxStamina,
            experience = experience,
            tasksCompleted = tasksCompleted,
            totalTasksForLevel = totalTasksForLevel,
            equippedItems = equippedItems
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
                    onCompleteTask = onCompleteTask,
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
            onNavigationBarVisibilityChange = { },
            maxStamina = 120
        )
    }
}



