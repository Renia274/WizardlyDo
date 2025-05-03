package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile

data class TaskUiState(
    val wizardProfile: Result<WizardProfile?>? = null,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val recentlyCreatedTask: Task? = null,
    val onFilterChange: ((TaskFilter) -> Unit)? = null,

    // Pagination support
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

enum class TaskFilter { ALL, ACTIVE, COMPLETED, DAILY }