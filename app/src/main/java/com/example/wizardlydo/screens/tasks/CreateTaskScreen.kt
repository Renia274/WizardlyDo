package com.example.wizardlydo.screens.tasks

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.CategorySelector
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.CreateTaskButton
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.DueDateSelector
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.PrioritySelector
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskDescriptionField
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.TaskTitleField
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.utilities.TaskNotificationService
import com.example.wizardlydo.viewmodel.tasks.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBack: () -> Unit,
    viewModel: TaskViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val taskNotificationService = remember { TaskNotificationService(context) }
    val coroutineScope = rememberCoroutineScope()

    val userId by viewModel.currentUserIdState.collectAsState()

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
                coroutineScope.launch {
                    viewModel.createTask(task)

                    taskNotificationService.showTaskCreatedNotification(task)

                    // If the task has a due date, schedule future reminder notifications
                    if (task.dueDate != null) {
                        taskNotificationService.scheduleTaskNotification(task)
                    }

                    onBack()
                }
            },
            userId = userId
        )
    }
}

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
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val categories = listOf("School", "Chores", "Work", "Personal")
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)
    val verticalSpacing = (screenHeight * 0.01f).coerceIn(4.dp, 8.dp)

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        object : PermissionState {
            override val permission: String = Manifest.permission.POST_NOTIFICATIONS
            override val status: PermissionStatus = PermissionStatus.Granted
            override fun launchPermissionRequest() { }
        }
    }

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
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing * 2)
    ) {
        Spacer(modifier = Modifier.height(verticalSpacing))

        TaskTitleField(title, onTitleChange = { title = it })
        TaskDescriptionField(description, onDescriptionChange = { description = it })
        DueDateSelector(
            dueDate,
            dateFormatter,
            onDatePickerTrigger = { showDatePicker = true }
        )
        PrioritySelector(priority, onPrioritySelected = { priority = it })
        CategorySelector(category, categories, onCategorySelected = { category = it })

        if (!notificationPermissionState.status.isGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalSpacing),
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

        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        CreateTaskButton(
            enabled = title.isNotBlank(),
            onClick = {
                userId?.let { safeUserId ->
                    val task = Task(
                        id = 0,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        category = category.ifEmpty { null },
                        userId = safeUserId,
                        isCompleted = false,
                        createdAt = System.currentTimeMillis()
                    )
                    onCreateTask(task)
                }
            }
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 2))
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTaskContentPreview() {
    WizardlyDoTheme {
        // Create a simplified version for preview without permissions
        CreateTaskContentForPreview()
    }
}

@Composable
private fun CreateTaskContentForPreview() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var category by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf("School", "Chores", "Work", "Personal")
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)
    val verticalSpacing = (screenHeight * 0.01f).coerceIn(4.dp, 8.dp)

    Column(
        modifier = Modifier
            .padding(PaddingValues(16.dp))
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing * 2)
    ) {
        Spacer(modifier = Modifier.height(verticalSpacing))

        TaskTitleField(title, onTitleChange = { title = it })
        TaskDescriptionField(description, onDescriptionChange = { description = it })
        DueDateSelector(
            dueDate,
            dateFormatter,
            onDatePickerTrigger = { showDatePicker = true }
        )
        PrioritySelector(priority, onPrioritySelected = { priority = it })
        CategorySelector(category, categories, onCategorySelected = { category = it })
        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        CreateTaskButton(
            enabled = title.isNotBlank(),
            onClick = {}
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 2))
    }
}