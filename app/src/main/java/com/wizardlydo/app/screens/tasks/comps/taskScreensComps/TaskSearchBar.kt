package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.R
import com.wizardlydo.app.data.models.TaskFilter
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    selectedPriority: Priority?,
    onPriorityChange: (Priority?) -> Unit,
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf(searchQuery) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        inputText = searchQuery
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        SearchBar(
            query = inputText,
            onQueryChange = { newText ->
                inputText = newText
                onSearchQueryChange(newText)
                // Activate search when user starts typing
                if (newText.isNotEmpty()) {
                    viewModel.activateSearch()
                }
            },
            onSearch = {
                viewModel.activateSearch()
            },
            active = false,
            onActiveChange = { active ->
                if (active && inputText.isNotEmpty()) {
                    viewModel.activateSearch()
                }
            },
            placeholder = {
                Text(
                    "Search tasks...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Row {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Toggle Filters",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        inputText = ""
                        onSearchQueryChange("")
                        showFilters = false
                        viewModel.deactivateSearch()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Query",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

        }

        if (showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPriority == null,
                        onClick = {
                            onPriorityChange(null)
                            viewModel.activateSearch()
                        },
                        label = { Text("Any") }
                    )

                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = {
                                onPriorityChange(priority)
                                viewModel.activateSearch()
                            },
                            label = { Text(priority.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when (priority) {
                                                Priority.LOW -> Color.Green
                                                Priority.MEDIUM -> Color(0xFFFFA500)
                                                Priority.HIGH -> Color.Red
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        )
                    }
                }

                Text(
                    text = "Task Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                onFilterChange(filter)
                                viewModel.activateSearch()
                            },
                            label = { Text(filter.name) }
                        )
                    }
                }
            }
        }
    }
}