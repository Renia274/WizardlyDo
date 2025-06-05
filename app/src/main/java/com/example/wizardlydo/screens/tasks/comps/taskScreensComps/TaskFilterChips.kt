package com.example.wizardlydo.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.models.TaskFilter

@Composable
fun TaskFilterChips(
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        FilterChip(
            selected = currentFilter == TaskFilter.ALL,
            onClick = { onFilterChange(TaskFilter.ALL) },
            label = { Text("ALL") }
        )

        FilterChip(
            selected = currentFilter == TaskFilter.ACTIVE,
            onClick = { onFilterChange(TaskFilter.ACTIVE) },
            label = { Text("ACTIVE") }
        )

        FilterChip(
            selected = currentFilter == TaskFilter.COMPLETED,
            onClick = { onFilterChange(TaskFilter.COMPLETED) },
            label = { Text("COMPLETED") }
        )
    }
}