package com.example.wizardlydo.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.viewmodel.tasks.TaskViewModel

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
    var expanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(searchQuery) }


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
            },
            onSearch = {
                // Apply search on submit
                viewModel.activateSearch()
                expanded = false
            },
            active = expanded,
            onActiveChange = { isActive ->
                expanded = isActive

                if (!isActive && inputText.isNotEmpty()) {
                    viewModel.activateSearch()
                }
            },
            placeholder = { Text("Search tasks...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                IconButton(onClick = {

                    inputText = ""
                    onSearchQueryChange("")
                    viewModel.deactivateSearch()
                    onCloseSearch()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {


                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected = selectedPriority == null,
                        onClick = { onPriorityChange(null) },
                        label = { Text("Any") }
                    )


                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when (priority) {
                                                Priority.LOW -> Color.Green
                                                Priority.MEDIUM -> Color.Yellow
                                                Priority.HIGH -> Color.Red
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    text = "Task Type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.name) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.activateSearch()
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
