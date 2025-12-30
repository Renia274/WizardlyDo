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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wizardlydo.app.comps.ErrorMessage
import com.wizardlydo.app.comps.FullScreenLoading
import com.wizardlydo.app.models.TaskUiState
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.data.wizard.items.EquippedItems
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.EmptyTaskList
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.Level30CompletionDialog
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.LevelUpIndicator
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskBottomBar
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskListSection
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskSearchBar
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats.CharacterStatsSection
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats.LocalTaskViewModel
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

    // Track FAB visibility based on scroll
    var isFabVisible by remember { mutableStateOf(true) }

    val firstTaskId = state.filteredTasks.firstOrNull()?.id

    // Load data on initial composition AND when returning to screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(searchQuery, selectedPriority) {
        if (isSearchVisible) {
            viewModel.applySearchFilters(
                query = searchQuery,
                priority = selectedPriority,
                category = state.selectedCategory
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

    // Provide TaskViewModel through CompositionLocal
    CompositionLocalProvider(LocalTaskViewModel provides viewModel) {
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
                    visible = isUIVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    TaskBottomBar(
                        onEdit = {
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
                AnimatedVisibility(
                    visible = isUIVisible && isFabVisible,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
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
                // LANDSCAPE MODE
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left side - Character stats
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CharacterStatsSection(
                            wizardResult = state.wizardProfile,
                            health = wizardProfile?.health ?: 100,
                            maxHealth = wizardProfile?.maxHealth ?: 100,
                            stamina = wizardProfile?.stamina ?: 50,
                            maxStamina = wizardProfile?.maxStamina ?: 100,
                            experience = wizardProfile?.experience ?: 0,
                            level = wizardProfile?.level ?: 1,
                            equippedItems = equippedItems,
                            recentDamage = state.recentDamage
                        )

                        wizardProfile?.let {
                            LevelUpIndicator(it.level)
                        }
                    }

                    // Right side - Tasks
                    Column(modifier = Modifier.weight(0.6f)) {
                        // Task list
                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                state.error != null -> ErrorMessage(error = state.error)
                                state.isLoading -> FullScreenLoading()
                                state.filteredTasks.isEmpty() && viewModel.getIncompleteTutorialTasks().isEmpty() -> EmptyTaskList()
                                else -> TaskListSection(
                                    tutorialTasks = viewModel.getIncompleteTutorialTasks(),
                                    tasks = state.filteredTasks,
                                    currentPage = state.currentPage,
                                    totalPages = state.totalPages,
                                    onNextPage = { viewModel.nextPage() },
                                    onPreviousPage = { viewModel.previousPage() },
                                    onCompleteTask = { taskId ->
                                        if (taskId < 0) {
                                            viewModel.completeTutorialTask(taskId)
                                        } else if (viewModel.isWizardDefeated()) {
                                            viewModel.updateRevivalProgress(taskId) {}
                                        } else {
                                            viewModel.completeTask(taskId, null)
                                        }
                                    },
                                    onEditTask = { taskId ->
                                        if (taskId >= 0) {
                                            onEditTask(taskId)
                                        }
                                    },
                                    onDeleteTask = { taskId ->
                                        if (taskId >= 0) {
                                            viewModel.deleteTaskWithDamage(taskId) {}
                                        }
                                    },
                                    onScrollStateChanged = { isScrollingDown ->
                                        isFabVisible = !isScrollingDown
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // PORTRAIT MODE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
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
                                        level = wizardProfile?.level ?: 1,
                                        equippedItems = equippedItems,
                                        recentDamage = state.recentDamage,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .border(
                                                width = if (isBeingSwiped) 3.dp else 0.dp,
                                                color = if (isBeingSwiped) MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.8f
                                                ) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .background(
                                                color = if (isBeingSwiped) MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.1f
                                                ) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = { isBeingSwiped = true },
                                                    onDragEnd = {
                                                        isBeingSwiped = false
                                                        isUIVisible = false
                                                    }
                                                ) { change, dragAmount ->
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
                            }
                        }

                        // Pull-down indicator when UI is hidden
                        if (!isUIVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragEnd = { isUIVisible = true }
                                        ) { change, dragAmount ->
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
                                        containerColor = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.3f
                                        )
                                    ),
                                    shape = RoundedCornerShape(2.dp)
                                ) {}
                            }
                        }
                    }

                    // Task list
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            state.error != null -> ErrorMessage(error = state.error)
                            state.isLoading -> FullScreenLoading()
                            state.filteredTasks.isEmpty() && viewModel.getIncompleteTutorialTasks().isEmpty() -> EmptyTaskList()
                            else -> TaskListSection(
                                tutorialTasks = viewModel.getIncompleteTutorialTasks(),
                                tasks = state.filteredTasks,
                                currentPage = state.currentPage,
                                totalPages = state.totalPages,
                                onNextPage = { viewModel.nextPage() },
                                onPreviousPage = { viewModel.previousPage() },
                                onCompleteTask = { taskId ->
                                    if (taskId < 0) {
                                        viewModel.completeTutorialTask(taskId)
                                    } else if (viewModel.isWizardDefeated()) {
                                        viewModel.updateRevivalProgress(taskId) {}
                                    } else {
                                        viewModel.completeTask(taskId, null)
                                    }
                                },
                                onEditTask = { taskId ->
                                    if (taskId >= 0) {
                                        onEditTask(taskId)
                                    }
                                },
                                onDeleteTask = { taskId ->
                                    if (taskId >= 0) {
                                        viewModel.deleteTaskWithDamage(taskId) {}
                                    }
                                },
                                onScrollStateChanged = { isScrollingDown ->
                                    isFabVisible = !isScrollingDown
                                }
                            )
                        }
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
    level: Int,
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
            level = level,
            equippedItems = equippedItems
        )

        wizardProfile?.let { LevelUpIndicator(it.level) }

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
        maxHealth = 150,
        stamina = 75,
        maxStamina = 120,
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
            isCompleted = index % 4 == 0,
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
            modifier = Modifier,
            state = TaskUiState(
                isLoading = false,
                wizardProfile = Result.success(wizardProfile),
                tasks = sampleTasks,
                filteredTasks = paginatedTasks,
                currentPage = 1,
                totalPages = 3
            ),
            wizardProfile = wizardProfile,
            health = 100,
            maxHealth = 150,
            stamina = 75,
            maxStamina = 120,
            experience = 250,
            level = 5,
            equippedItems = null,
            onCompleteTask = {},
            onEditTask = {},
            onDeleteTask = {},
            onNextPage = {},
            onPreviousPage = {},
            onDamageTaken = { _, _ -> }
        )
    }
}