package com.wizardlydo.app.screens.tasks

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.models.EditTaskField
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.CategorySelector
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.DueDateSelector
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.PrioritySelector
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskDescriptionField
import com.wizardlydo.app.screens.tasks.comps.taskScreensComps.TaskTitleField
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    onBack: () -> Unit,
    viewModel: TaskViewModel = koinViewModel()
) {
    val editState by viewModel.editTaskState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        viewModel.loadTaskForEditing(taskId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetEditTaskState()
        }
    }

    LaunchedEffect(editState.error) {
        editState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearEditTaskError()
        }
    }

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
                isSaving = editState.isSaving,
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
                onSaveClick = {
                    viewModel.saveEditedTask(onSuccess = {
                        Toast.makeText(context, "Task saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    })
                },
                padding = padding
            )
        }
    }
}

@Composable
fun EditTaskScreenContent(
    title: String,
    description: String,
    dueDate: Long?,
    priority: Priority,
    category: String,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDueDateSelect: () -> Unit,
    onPrioritySelect: (Priority) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSaveClick: () -> Unit,
    padding: PaddingValues
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val horizontalPadding = (screenWidth * 0.04f).coerceIn(16.dp, 32.dp)
    val verticalSpacing = (screenHeight * 0.02f).coerceIn(16.dp, 24.dp)
    val maxContentWidth = if (screenWidth > 600.dp) 600.dp else screenWidth

    val categories = listOf("School", "Chores", "Work", "Personal")
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxContentWidth)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            TaskTitleField(title, onTitleChange)
            TaskDescriptionField(description, onDescriptionChange)
            DueDateSelector(
                dueDate,
                dateFormatter,
                onDatePickerTrigger = onDueDateSelect
            )
            PrioritySelector(priority, onPrioritySelect)
            CategorySelector(category, categories, onCategorySelect)

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && !isSaving
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
        }
    }
}

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
            isSaving = false,
            onTitleChange = {},
            onDescriptionChange = {},
            onDueDateSelect = {},
            onPrioritySelect = {},
            onCategorySelect = {},
            onSaveClick = {},
            padding = PaddingValues(0.dp)
        )
    }
}