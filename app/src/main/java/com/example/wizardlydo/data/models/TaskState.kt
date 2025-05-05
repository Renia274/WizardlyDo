package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task
import com.example.wizardlydo.data.wizard.WizardProfile

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
    val totalPages: Int = 1,

    // Search parameters
    val searchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val taskType: TaskFilter = TaskFilter.ALL
)

enum class TaskFilter { ALL, ACTIVE, COMPLETED, DAILY }