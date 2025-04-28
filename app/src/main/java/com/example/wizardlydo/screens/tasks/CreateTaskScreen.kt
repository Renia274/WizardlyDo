package com.example.wizardlydo.screens.tasks

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.screens.tasks.comps.CategorySelector
import com.example.wizardlydo.screens.tasks.comps.CreateTaskButton
import com.example.wizardlydo.screens.tasks.comps.DailyTaskToggle
import com.example.wizardlydo.screens.tasks.comps.DueDateSelector
import com.example.wizardlydo.screens.tasks.comps.PrioritySelector
import com.example.wizardlydo.screens.tasks.comps.TaskDescriptionField
import com.example.wizardlydo.screens.tasks.comps.TaskTitleField
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.SettingsViewModel
import com.example.wizardlydo.viewmodel.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBack: () -> Unit,
    viewModel: TaskViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val userId by viewModel.currentUserIdState.collectAsState()
    val context = LocalContext.current
    val taskNotificationService = remember { TaskNotificationService(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        CreateTaskContent(
            padding = paddingValues,
            onCreateTask = { task ->
                viewModel.createTask(task)



                // Show system notification with expandable content
                taskNotificationService.showTaskCreatedNotification(task)

                // If the task has a due date, schedule future reminder notifications
                if (task.dueDate != null) {
                    taskNotificationService.scheduleTaskNotification(task)
                }

                onBack() // Navigate back after creating task
            },
            userId = viewModel.currentUserIdState.value
        )
    }
}

@SuppressLint("InlinedApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateTaskContent(
    padding: PaddingValues,
    onCreateTask: (Task) -> Unit,
    userId: String?
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIUM) }
    var category by rememberSaveable { mutableStateOf("") }
    var isDaily by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val categories = listOf("School", "Chores", "Work", "Personal")
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Check notification permission at compose time
    val notificationPermissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )

    // Request notification permission if needed
    LaunchedEffect(Unit) {
        if (!notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskTitleField(title, onTitleChange = { title = it })
        TaskDescriptionField(description, onDescriptionChange = { description = it })
        DueDateSelector(
            dueDate,
            dateFormatter,
            onDatePickerTrigger = { showDatePicker = true }
        )
        PrioritySelector(priority, onPrioritySelected = { priority = it })
        CategorySelector(category, categories, onCategorySelected = { category = it })
        DailyTaskToggle(isDaily, onDailyChanged = { isDaily = it })

        // Notification permission info (if not granted)
        if (!notificationPermissionState.status.isGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Notification Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Enable notifications to get reminders for your tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CreateTaskButton(
            enabled = title.isNotBlank(),
            onClick = {
                userId?.let { safeUserId ->
                    val task = Task(
                        id = 0, // Will be generated by Room
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        category = category.ifEmpty { null },
                        isDaily = isDaily,
                        userId = safeUserId,
                        isCompleted = false,
                        createdAt = System.currentTimeMillis()
                    )
                    onCreateTask(task)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTaskContentPreview() {
    WizardlyDoTheme {
        CreateTaskContent(
            padding = PaddingValues(0.dp),
            onCreateTask = {},
            userId = "test-user-123"
        )
    }
}