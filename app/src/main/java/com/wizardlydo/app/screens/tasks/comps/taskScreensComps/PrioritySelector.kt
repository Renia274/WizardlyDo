package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wizardlydo.app.data.tasks.Priority


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
                        Priority.HIGH -> Color.Red.copy(alpha = 0.2f)
                        Priority.MEDIUM -> Color(0xFFFFA500).copy(alpha = 0.2f) // Orange
                        Priority.LOW -> Color.Green.copy(alpha = 0.2f)
                    },
                    selectedLabelColor = when (priorityOption) {
                        Priority.HIGH -> Color.Red
                        Priority.MEDIUM -> Color(0xFFFFA500) // Orange
                        Priority.LOW -> Color.Green
                    }
                )
            )
        }
    }
}