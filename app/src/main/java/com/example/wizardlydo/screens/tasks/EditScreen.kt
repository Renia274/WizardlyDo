
package com.example.wizardlydo.screens.tasks

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.wizardlydo.data.models.EditTaskField
import com.example.wizardlydo.screens.tasks.comps.CategorySelector
import com.example.wizardlydo.screens.tasks.comps.DailyTaskToggle
import com.example.wizardlydo.screens.tasks.comps.DueDateSelector
import com.example.wizardlydo.screens.tasks.comps.PrioritySelector
import com.example.wizardlydo.screens.tasks.comps.TaskDescriptionField
import com.example.wizardlydo.screens.tasks.comps.TaskTitleField
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.TaskViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen for editing an existing task.
 *
 * @param taskId ID of the task to edit
 * @param onBack Callback for navigating back
 * @param viewModel The TaskViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    onBack: () -> Unit,
    viewModel: TaskViewModel = koinViewModel()
) {
    val editState by viewModel.editTaskState.collectAsState()
    val context = LocalContext.current

    // Initial task loading
    LaunchedEffect(taskId) {
        viewModel.loadTaskForEditing(taskId)
    }

    // Reset state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetEditTaskState()
        }
    }

    // Handle errors with Toast
    LaunchedEffect(editState.error) {
        editState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearEditTaskError()
        }
    }

    // Date picker state
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editState.dueDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.updateEditTaskField(
                                EditTaskField.DUE_DATE,
                                it
                            )
                        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
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
    ) { padding ->
        if (editState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            EditTaskScreenContent(
                title = editState.title,
                description = editState.description,
                dueDate = editState.dueDate,
                priority = editState.priority,
                category = editState.category,
                isDaily = editState.isDaily,
                isSaving = editState.isSaving,
                isDeleting = editState.isDeleting,
                onTitleChange = {
                    viewModel.updateEditTaskField(EditTaskField.TITLE, it)
                },
                onDescriptionChange = {
                    viewModel.updateEditTaskField(EditTaskField.DESCRIPTION, it)
                },
                onDueDateSelect = { showDatePicker = true },
                onPrioritySelect = {
                    viewModel.updateEditTaskField(EditTaskField.PRIORITY, it)
                },
                onCategorySelect = {
                    viewModel.updateEditTaskField(EditTaskField.CATEGORY, it)
                },
                onDailyChange = {
                    viewModel.updateEditTaskField(EditTaskField.IS_DAILY, it)
                },
                onSaveClick = {
                    viewModel.saveEditedTask(onSuccess = onBack)
                },
                onDeleteClick = {
                    viewModel.deleteTask(taskId, onSuccess = onBack)
                },
                padding = padding
            )
        }
    }
}

/**
 * Content of the Edit Task screen.
 *
 * @param title Current task title
 * @param description Current task description
 * @param dueDate Current task due date
 * @param priority Current task priority
 * @param category Current task category
 * @param isDaily Whether the task is daily
 * @param isSaving Whether the task is currently being saved
 * @param isDeleting Whether the task is currently being deleted
 * @param onTitleChange Callback when title changes
 * @param onDescriptionChange Callback when description changes
 * @param onDueDateSelect Callback when due date selection is requested
 * @param onPrioritySelect Callback when priority is selected
 * @param onCategorySelect Callback when category is selected
 * @param onDailyChange Callback when daily status changes
 * @param onSaveClick Callback when save button is clicked
 * @param onDeleteClick Callback when delete button is clicked
 * @param padding Padding values from scaffold
 */
@Composable
fun EditTaskScreenContent(
    title: String,
    description: String,
    dueDate: Long?,
    priority: Priority,
    category: String,
    isDaily: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDueDateSelect: () -> Unit,
    onPrioritySelect: (Priority) -> Unit,
    onCategorySelect: (String) -> Unit,
    onDailyChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    padding: PaddingValues
) {
    val categories = listOf("School", "Chores", "Work", "Personal")
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskTitleField(title, onTitleChange)
        TaskDescriptionField(description, onDescriptionChange)
        DueDateSelector(
            dueDate,
            dateFormatter,
            onDatePickerTrigger = onDueDateSelect
        )
        PrioritySelector(priority, onPrioritySelect)
        CategorySelector(category, categories, onCategorySelect)
        DailyTaskToggle(isDaily, onDailyChange)

        // Save button
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxSize(),
            enabled = title.isNotBlank() && !isSaving && !isDeleting
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Changes")
        }

        // Delete button
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            enabled = !isDeleting && !isSaving
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.error,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text("Delete Task")
        }
    }
}

/**
 * Preview of the EditTaskScreen content.
 */
@Preview(showBackground = true)
@Composable
fun EditTaskScreenContentPreview() {
    WizardlyDoTheme {
        EditTaskScreenContent(
            title = "Complete Homework",
            description = "Math and science homework due tomorrow",
            dueDate = System.currentTimeMillis() + 86400000, // Tomorrow
            priority = Priority.HIGH,
            category = "School",
            isDaily = false,
            isSaving = false,
            isDeleting = false,
            onTitleChange = {},
            onDescriptionChange = {},
            onDueDateSelect = {},
            onPrioritySelect = {},
            onCategorySelect = {},
            onDailyChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            padding = PaddingValues(0.dp)
        )
    }
}