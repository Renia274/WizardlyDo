package com.example.wizardlydo.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wizardlydo.data.tasks.Priority


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