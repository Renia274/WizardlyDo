package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent


class TaskViewModel (
    private val taskRepository: TaskRepository,
    private val wizardRepository: WizardRepository
) : ViewModel(),KoinComponent {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    _uiState.update { it.copy(error = "Not logged in", isLoading = false) }
                    return@launch
                }

                // Get wizard profile with proper error handling
                val wizardResult = runCatching {
                    wizardRepository.getWizardProfile(userId).getOrThrow()
                }

                // Get tasks with error handling
                val tasks = runCatching {
                    taskRepository.getAllTasks(userId.toInt())
                }.getOrElse {
                    _uiState.update { it.copy(error = "Failed to load tasks") }
                    emptyList()
                }

                val filteredTasks = filterTasks(tasks, _uiState.value.currentFilter)

                _uiState.update {
                    it.copy(
                        wizardProfile = wizardResult.fold(
                            onSuccess = { Result.success(it) },
                            onFailure = { Result.failure(it) }
                        ),
                        tasks = tasks,
                        filteredTasks = filteredTasks,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load: ${e.message}",
                        isLoading = false,
                        wizardProfile = Result.failure(e)
                    )
                }
            }
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskRepository.updateTaskCompletionStatus(taskId, true)
                loadData() // Refresh data after completion
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to complete task: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.DAILY -> tasks.filter { it.isDaily }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { state ->
            state.copy(
                currentFilter = filter,
                filteredTasks = filterTasks(state.tasks, filter)
            )
        }


    }
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}