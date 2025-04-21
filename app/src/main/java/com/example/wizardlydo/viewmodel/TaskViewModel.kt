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

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val wizardRepository: WizardRepository
) : ViewModel(), KoinComponent {

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

                val wizard = wizardRepository.getWizardProfile(userId)
                // Assuming userId is now properly typed as Int
                val tasks = taskRepository.getAllTasks(userId.toInt())
                val filteredTasks = filterTasks(tasks, _uiState.value.currentFilter)

                _uiState.update {
                    it.copy(
                        wizardProfile = wizard,
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
                        wizardProfile = null
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

                // Refresh task data
                val userId = wizardRepository.getCurrentUserId() ?: return@launch

                // Assuming userId is now properly typed as Int
                val updatedTasks = taskRepository.getAllTasks(userId.toInt())
                val filteredTasks = filterTasks(updatedTasks, _uiState.value.currentFilter)

                // Refresh wizard data after completion
                val updatedWizard = wizardRepository.getWizardProfile(userId)
                _uiState.update {
                    it.copy(
                        wizardProfile = updatedWizard,
                        tasks = updatedTasks,
                        filteredTasks = filteredTasks,
                        isLoading = false
                    )
                }
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
        viewModelScope.launch {
            val filteredTasks = filterTasks(_uiState.value.tasks, filter)
            _uiState.update {
                it.copy(
                    currentFilter = filter,
                    filteredTasks = filteredTasks
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}