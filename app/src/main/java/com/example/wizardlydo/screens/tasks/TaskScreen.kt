package com.example.wizardlydo.screens.tasks

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.wizardlydo.comps.InAppNotification
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.screens.tasks.comps.CharacterStatsSection
import com.example.wizardlydo.screens.tasks.comps.EmptyTaskList
import com.example.wizardlydo.screens.tasks.comps.ErrorMessage
import com.example.wizardlydo.screens.tasks.comps.FullScreenLoading
import com.example.wizardlydo.screens.tasks.comps.TaskBottomBar
import com.example.wizardlydo.screens.tasks.comps.TaskFilterChips
import com.example.wizardlydo.screens.tasks.comps.TaskListSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.SettingsViewModel
import com.example.wizardlydo.viewmodel.TaskViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onHome: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onEditMode: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notification by settingsViewModel.activeNotification.collectAsState()
    val notificationPermissionGranted by settingsViewModel.notificationPermissionGranted.collectAsState()
    val appSettings by settingsViewModel.state.collectAsState()

    // Remember the last created task info
    val lastCreatedTask = remember { mutableStateOf<Task?>(null) }

    // Load data when screen is composed
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Check if we need to show a task created notification
    LaunchedEffect(state.tasks) {
        if (state.tasks.isNotEmpty() &&
            notificationPermissionGranted &&
            appSettings.inAppNotificationsEnabled) {

            viewModel.getRecentlyCreatedTask()?.let { task ->
                settingsViewModel.showNotification(
                    SettingsViewModel.InAppNotificationData.Info(
                        message = "Task \"${task.title}\" created successfully!" +
                                (task.dueDate?.let {
                                    " (due on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))})"
                                } ?: ""),
                        duration = 5000
                    )
                )
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.matchParentSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Task Manager") }
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
                FloatingActionButton(
                    onClick = onCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task")
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    state.isLoading -> FullScreenLoading()
                    else -> TaskContent(
                        state = state,
                        onEditTask = onEditTask,
                        onCompleteTask = { taskId ->
                            viewModel.completeTask(taskId)
                            // Only show notification if enabled
                            if (appSettings.inAppNotificationsEnabled && notificationPermissionGranted) {
                                settingsViewModel.activeNotificationFlow.value =
                                    SettingsViewModel.InAppNotificationData.Info(
                                        message = "Task completed! +50 XP"
                                    )
                            }
                        },
                        onDeleteTask = { taskId ->
                            viewModel.deleteTask(taskId) {
                                // Only show notification if enabled
                                if (appSettings.inAppNotificationsEnabled && notificationPermissionGranted) {
                                    settingsViewModel.activeNotificationFlow.value =
                                        SettingsViewModel.InAppNotificationData.Warning(
                                            message = "Task deleted!"
                                        )
                                }
                            }
                        },
                        onDamageTaken = { damage, currentHealth ->
                            // Only show notification if enabled and damage notifications enabled
                            if (appSettings.inAppNotificationsEnabled &&
                                appSettings.damageNotificationsEnabled &&
                                notificationPermissionGranted) {
                                settingsViewModel.activeNotificationFlow.value =
                                    SettingsViewModel.InAppNotificationData.Warning(
                                        message = "Damage taken! $damage HP lost! Health: $currentHealth"
                                    )
                            }
                        }
                    )
                }
            }
        }

        // Display the notification at the top layer
        notification?.let { notif ->
            InAppNotification(
                message = notif.message,
                type = notif.type,
                onDismiss = { settingsViewModel.clearNotification() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .zIndex(10f) // Ensure it's on top of everything
            )
        }
    }
}

@Composable
fun TaskContent(
    state: TaskUiState,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onDamageTaken: (damage: Int, currentHealth: Int) -> Unit = { _, _ -> },
) {
    val wizardProfile = state.wizardProfile?.getOrNull()

    Column(Modifier.fillMaxSize()) {
        CharacterStatsSection(
            wizardResult = state.wizardProfile,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        TaskFilterChips(
            currentFilter = state.currentFilter,
            onFilterChange = { filter ->
                state.onFilterChange?.invoke(filter)
            }
        )

        Spacer(Modifier.height(8.dp))

        when {
            state.error != null -> ErrorMessage(error = state.error)
            state.isLoading -> FullScreenLoading()
            state.filteredTasks.isEmpty() -> EmptyTaskList()
            else -> TaskListSection(
                tasks = state.filteredTasks,
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
                }
            )
        }

        Spacer(Modifier.weight(1f))
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
                        createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
                        isDaily = false,
                        category = "School"
                    ),
                    Task(
                        id = 2,
                        title = "Go shopping",
                        description = "Buy groceries",
                        dueDate = System.currentTimeMillis() + 86400000, // 1 day from now
                        priority = Priority.MEDIUM,
                        userId = "",
                        isCompleted = true,
                        createdAt = System.currentTimeMillis() - 172800000, // 2 days ago
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