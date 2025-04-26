package com.example.wizardlydo.screens.tasks

import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.wizardlydo.viewmodel.TaskViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel(),
    onHome: () -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onEditMode: () -> Unit,
    onSettings: () -> Unit,
    onCompleteTask: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load data when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Show toast messages for errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
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
        when {
            state.isLoading -> FullScreenLoading()
            else -> TaskContent(
                state = state.copy(onFilterChange = viewModel::setFilter),
                onEditTask = onEditTask,
                onCompleteTask = onCompleteTask,
                onDeleteTask = { taskId ->
                    viewModel.deleteTask(taskId) {
                        // Success callback - already handled in viewModel
                    }
                },
                modifier = Modifier.padding(padding)
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        CharacterStatsSection(
            wizardResult = state.wizardProfile,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TaskFilterChips(
            currentFilter = state.currentFilter,
            onFilterChange = { filter ->
                state.onFilterChange?.invoke(filter)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.error != null -> {
                ErrorMessage(error = state.error)
            }
            state.filteredTasks.isEmpty() -> {
                EmptyTaskList()
            }
            else -> {
                TaskListSection(
                    tasks = state.filteredTasks,
                    onCompleteTask = onCompleteTask,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
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
            onDeleteTask = {}
        )
    }
}