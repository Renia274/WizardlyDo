package com.wizardlydo.app.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.comps.ErrorMessage
import com.wizardlydo.app.comps.FullScreenLoading
import com.wizardlydo.app.data.models.TaskFilter
import com.wizardlydo.app.data.models.TaskUiState
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.data.wizard.items.EquippedItems
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.EmptyTaskList
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.Level30CompletionDialog
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.LevelUpIndicator
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskBottomBar
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskFilterChips
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskListSection
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskSearchBar
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats.CharacterStatsSection
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.inventory.InventoryViewModel
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel(),
    inventoryViewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onSettings: () -> Unit,
    onInventory: () -> Unit,
    onDonation: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val equippedItems by inventoryViewModel.equippedItemsFlow.collectAsState()
    val wizardProfile = state.wizardProfile?.getOrNull()
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }

    var isUIVisible by remember { mutableStateOf(true) }

    // Track the first task ID for the Edit button
    val firstTaskId = state.filteredTasks.firstOrNull()?.id

    LaunchedEffect(Unit) { viewModel.loadData() }

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

    if (state.showLevel30Dialog) {
        Level30CompletionDialog(
            onDismiss = { viewModel.hideLevel30Dialog() },
            onDonate = onDonation,
            wizardName = wizardProfile?.wizardName ?: "Wizard"
        )
    }

    Scaffold(
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
            // Hide bottom bar when UI is not visible
            AnimatedVisibility(
                visible = isUIVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                TaskBottomBar(
                    onEdit = {
                        // Navigate to edit first task if available
                        firstTaskId?.let { taskId ->
                            onEditTask(taskId)
                        }
                    },
                    onSearch = {
                        isSearchVisible = !isSearchVisible
                        if (isSearchVisible) {
                            viewModel.activateSearch()
                        } else {
                            searchQuery = ""
                            selectedPriority = null
                            viewModel.deactivateSearch()
                        }
                    },
                    onSettings = onSettings,
                    onInventory = onInventory,
                    onDonation = onDonation
                )
            }
        },
        floatingActionButton = {
            // Hide FAB when UI is not visible
            AnimatedVisibility(
                visible = isUIVisible,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(onClick = onCreateTask) {
                    Icon(Icons.Default.Add, "Create Task")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Task")
                }
            }
        }
    ) { padding ->

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(0.4f).verticalScroll(rememberScrollState())) {
                    CharacterStatsSection(
                        wizardResult = state.wizardProfile,
                        health = wizardProfile?.health ?: 100,
                        maxHealth = wizardProfile?.maxHealth ?: 100,
                        stamina = wizardProfile?.stamina ?: 50,
                        maxStamina = wizardProfile?.maxStamina ?: 100,
                        experience = wizardProfile?.experience ?: 0,
                        tasksCompleted = 0,
                        totalTasksForLevel = 10,
                        equippedItems = equippedItems
                    )
                    wizardProfile?.let { LevelUpIndicator(it.level) }
                }

                Column(modifier = Modifier.weight(0.6f)) {
                    if (!isSearchVisible) {
                        TaskFilterChips(state.currentFilter) { viewModel.setFilter(it) }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            state.error != null -> ErrorMessage(error = state.error)
                            state.isLoading -> FullScreenLoading()
                            state.filteredTasks.isEmpty() -> EmptyTaskList()
                            else -> TaskListSection(
                                tasks = state.filteredTasks,
                                currentPage = state.currentPage,
                                totalPages = state.totalPages,
                                onNextPage = { viewModel.nextPage() },
                                onPreviousPage = { viewModel.previousPage() },
                                onCompleteTask = { taskId ->
                                    if (viewModel.isWizardDefeated()) {
                                        viewModel.updateRevivalProgress(taskId) {}
                                    } else {
                                        viewModel.completeTask(taskId, null)
                                    }
                                },
                                onEditTask = onEditTask,
                                onDeleteTask = { viewModel.deleteTask(it) {} },
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                Column {
                    AnimatedVisibility(
                        visible = isUIVisible,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Column {
                            Box {
                                var isBeingSwiped by remember { mutableStateOf(false) }

                                CharacterStatsSection(
                                    wizardResult = state.wizardProfile,
                                    health = wizardProfile?.health ?: 100,
                                    maxHealth = wizardProfile?.maxHealth ?: 100,
                                    stamina = wizardProfile?.stamina ?: 50,
                                    maxStamina = wizardProfile?.maxStamina ?: 100,
                                    experience = wizardProfile?.experience ?: 0,
                                    tasksCompleted = 0,
                                    totalTasksForLevel = 10,
                                    equippedItems = equippedItems,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .border(
                                            width = if (isBeingSwiped) 3.dp else 0.dp,
                                            color = if (isBeingSwiped)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            color = if (isBeingSwiped)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    isBeingSwiped = true
                                                },
                                                onDragEnd = {
                                                    isBeingSwiped = false
                                                    // Hide on upward swipe
                                                    isUIVisible = false
                                                }
                                            ) { change, dragAmount ->
                                                // Only respond to upward swipes
                                                if (dragAmount.y < 0) {
                                                    change.consume()
                                                }
                                            }
                                        }
                                )
                            }

                            wizardProfile?.let {
                                LevelUpIndicator(level = it.level)
                            }

                            if (!isSearchVisible) {
                                TaskFilterChips(
                                    currentFilter = state.currentFilter,
                                    onFilterChange = { viewModel.setFilter(it) }
                                )
                            }
                        }
                    }

                    // Swipe indicator when stats are hidden
                    if (!isUIVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            // Toggle on any downward swipe from the top area
                                            isUIVisible = true
                                        }
                                    ) { change, dragAmount ->
                                        // Only respond to downward swipes
                                        if (dragAmount.y > 0) {
                                            change.consume()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(2.dp)
                            ) {}
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        state.error != null -> ErrorMessage(error = state.error)
                        state.isLoading -> FullScreenLoading()
                        state.filteredTasks.isEmpty() -> EmptyTaskList()
                        else -> TaskListSection(
                            tasks = state.filteredTasks,
                            currentPage = state.currentPage,
                            totalPages = state.totalPages,
                            onNextPage = { viewModel.nextPage() },
                            onPreviousPage = { viewModel.previousPage() },
                            onCompleteTask = { taskId ->
                                if (viewModel.isWizardDefeated()) {
                                    viewModel.updateRevivalProgress(taskId) {}
                                } else {
                                    viewModel.completeTask(taskId, null)
                                }
                            },
                            onEditTask = onEditTask,
                            onDeleteTask = { viewModel.deleteTask(it) {} },
                        )
                    }
                }
            }
        }
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
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)
    val verticalSpacing = (screenHeight * 0.01f).coerceIn(4.dp, 8.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
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
            maxStamina = 120
        )
    }
}



