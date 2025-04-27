package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile

data class TaskUiState(
    val wizardProfile: Result<WizardProfile?>? = null,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filteredTasks: List<Task> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val onFilterChange: ((TaskFilter) -> Unit)? = null,
    val recentlyCreatedTask: Task? = null,
    val recentlyDeletedTask: Task? = null,
)

enum class TaskFilter { ALL, ACTIVE, COMPLETED, DAILY }