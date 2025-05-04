package com.example.wizardlydo.screens.tasks.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.Priority
import java.text.DateFormat
import java.util.Date

@Composable
fun TaskTitleField(
    title: String,
    onTitleChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Task Title") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun TaskDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        )
    )
}

@Composable
fun DueDateSelector(
    dueDate: Long?,
    dateFormatter: DateFormat,
    onDatePickerTrigger: () -> Unit
) {
    OutlinedButton(
        onClick = onDatePickerTrigger,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(dueDate?.let { "Due: ${dateFormatter.format(Date(it))}" } ?: "Select Due Date")
    }
}

@Composable
fun PrioritySelector(
    priority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Text(
        "Priority",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Priority.entries.forEach { priorityOption ->
            FilterChip(
                selected = priority == priorityOption,
                onClick = { onPrioritySelected(priorityOption) },
                label = { Text(priorityOption.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (priorityOption) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        Priority.LOW -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    },
                    selectedLabelColor = when (priorityOption) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                        Priority.LOW -> MaterialTheme.colorScheme.tertiary
                    }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    category: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category (optional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { categoryOption ->
                DropdownMenuItem(
                    text = { Text(categoryOption) },
                    onClick = {
                        onCategorySelected(categoryOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DailyTaskToggle(
    isDaily: Boolean,
    onDailyChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Make this a daily task",
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = isDaily,
            onCheckedChange = onDailyChanged
        )
    }
}

@Composable
fun CreateTaskButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            "Create Task",
            style = MaterialTheme.typography.titleMedium
        )
    }
}