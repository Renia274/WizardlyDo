package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.EditTaskField
import com.example.wizardlydo.data.models.EditTaskState
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.lang.Exception

@KoinViewModel
class TaskViewModel(
    private val taskRepository: TaskRepository,
    auth: FirebaseAuth = Firebase.auth,
    private val wizardRepository: WizardRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(TaskUiState())
    val uiState = mutableState.asStateFlow()

    val currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserIdState = currentUserId.asStateFlow()

    private val mutableEditTaskState = MutableStateFlow(EditTaskState())
    val editTaskState = mutableEditTaskState.asStateFlow()

    // Track if a task was recently created
    private var taskRecentlyCreated = false
    private var recentlyCreatedTaskId: Int? = null
    private val recentTaskTimeThreshold = 40000 // 10 seconds in milliseconds
    private var lastCreationTime = 0L

    companion object {
        private const val EXP_PER_LEVEL = 1000
    }

    init {
        loadData()
    }

    fun getRecentlyCreatedTask(): Task? {
        val taskId = recentlyCreatedTaskId ?: return null
        val currentTime = System.currentTimeMillis()

        // Only consider it "recent" if created within the threshold time
        if (currentTime - lastCreationTime > recentTaskTimeThreshold) {
            recentlyCreatedTaskId = null
            return null
        }

        return uiState.value.tasks.find { it.id == taskId }
    }
    fun createTask(task: Task) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                try {
                    val createdTask = task.copy(userId = userId)
                    taskRepository.insertTask(createdTask)
                    // Update state with the newly created task
                    mutableState.update {
                        it.copy(recentlyCreatedTask = createdTask)
                    }
                    loadData() // Refresh the task list
                } catch (e: Exception) {
                    mutableState.update {
                        it.copy(error = "Failed to create task: ${e.message}")
                    }
                }
            }
        }
    }

    // Add this to reset the recently created task state
    fun resetRecentlyCreatedTask() {
        mutableState.update { it.copy(recentlyCreatedTask = null) }
    }

    fun loadData() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }

            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in", isLoading = false) }
                    return@launch
                }

                val wizardResult = runCatching {
                    wizardRepository.getWizardProfile(userId).getOrThrow()
                }

                val tasks = runCatching {
                    taskRepository.getAllTasks(userId)
                }.fold(
                    onSuccess = { it },
                    onFailure = {
                        mutableState.update { it.copy(error = "Failed to load tasks") }
                        emptyList()
                    }
                )

                val filteredTasks = filterTasks(tasks, uiState.value.currentFilter)

                mutableState.update { it ->
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
                mutableState.update {
                    it.copy(
                        error = "Failed to load: ${e.message}",
                        isLoading = false,
                        wizardProfile = Result.failure(e)
                    )
                }
            }
        }
    }

    fun loadTaskForEditing(taskId: Int) {
        viewModelScope.launch {
            mutableEditTaskState.update { it.copy(isLoading = true) }

            try {
                // First try to find in current list
                val foundTask = uiState.value.tasks.find { it.id == taskId }
                if (foundTask != null) {
                    mutableEditTaskState.update {
                        it.copy(
                            task = foundTask,
                            title = foundTask.title,
                            description = foundTask.description,
                            dueDate = foundTask.dueDate,
                            priority = foundTask.priority,
                            category = foundTask.category ?: "",
                            isDaily = foundTask.isDaily,
                            isLoading = false
                        )
                    }
                } else {
                    // If not found in current list, fetch directly from repository
                    val loadedTask = withContext(Dispatchers.IO) {
                        taskRepository.getTaskById(taskId)
                    }

                    if (loadedTask != null) {
                        mutableEditTaskState.update {
                            it.copy(
                                task = loadedTask,
                                title = loadedTask.title,
                                description = loadedTask.description,
                                dueDate = loadedTask.dueDate,
                                priority = loadedTask.priority,
                                category = loadedTask.category ?: "",
                                isDaily = loadedTask.isDaily,
                                isLoading = false
                            )
                        }
                    } else {
                        mutableEditTaskState.update {
                            it.copy(
                                error = "Task not found",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                mutableEditTaskState.update {
                    it.copy(
                        error = "Failed to load task: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateEditTaskField(field: EditTaskField, value: Any) {
        mutableEditTaskState.update { state ->
            when (field) {
                EditTaskField.TITLE -> state.copy(title = value as String)
                EditTaskField.DESCRIPTION -> state.copy(description = value as String)
                EditTaskField.DUE_DATE -> state.copy(dueDate = value as Long?)
                EditTaskField.PRIORITY -> state.copy(priority = value as Priority)
                EditTaskField.CATEGORY -> state.copy(category = value as String)
                EditTaskField.IS_DAILY -> state.copy(isDaily = value as Boolean)
            }
        }
    }

    fun saveEditedTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = mutableEditTaskState.value
            val currentTask = currentState.task ?: return@launch

            mutableEditTaskState.update { it.copy(isSaving = true) }

            try {
                val updatedTask = currentTask.copy(
                    title = currentState.title,
                    description = currentState.description,
                    dueDate = currentState.dueDate,
                    priority = currentState.priority,
                    category = currentState.category.ifEmpty { null },
                    isDaily = currentState.isDaily
                )

                taskRepository.updateTask(updatedTask)
                loadData() // Refresh the main task list
                mutableEditTaskState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                mutableEditTaskState.update {
                    it.copy(
                        error = "Failed to update task: ${e.message}",
                        isSaving = false
                    )
                }
            }
        }
    }

    fun deleteTask(taskId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val task = uiState.value.tasks.find { it.id == taskId }
                    ?: taskRepository.getTaskById(taskId)

                if (task != null) {
                    taskRepository.deleteTask(task)
                    loadData() // refresh list
                    onSuccess()
                } else {
                    mutableState.update { it.copy(error = "Task not found") }
                }
            } catch (e: Exception) {
                mutableState.update { it.copy(error = "Failed to delete task: ${e.message}") }
            }
        }
    }


    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in", isLoading = false) }
                    return@launch
                }

                val task = taskRepository.getTaskById(taskId)
                val wizardProfile = wizardRepository.getWizardProfile(userId).getOrThrow()

                task?.let {
                    val now = System.currentTimeMillis()
                    val isOnTime = it.dueDate?.let { dueDate -> now <= dueDate } ?: true

                    val (hpChange, staminaChange, expGain) = calculateTaskEffects(
                        priority = it.priority,
                        isOnTime = isOnTime
                    )


                    val updatedProfile = wizardProfile?.copy(
                        health = (wizardProfile.health + hpChange).coerceIn(0, wizardProfile.maxHealth),
                        stamina = (wizardProfile.stamina + staminaChange).coerceIn(0, 100),
                        experience = wizardProfile.experience + expGain
                    )?.checkLevelUp() ?: run {
                        throw IllegalStateException("Wizard profile was null after successful retrieval")
                    }

                    wizardRepository.updateWizardProfile(userId, updatedProfile)
                    taskRepository.updateTaskCompletionStatus(taskId, true)

                    loadData()
                }
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Completion failed: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        mutableState.update { state ->
            state.copy(
                currentFilter = filter,
                filteredTasks = filterTasks(state.tasks, filter)
            )
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    fun clearEditTaskError() {
        mutableEditTaskState.update { it.copy(error = null) }
    }

    fun resetEditTaskState() {
        mutableEditTaskState.value = EditTaskState()
    }

    private fun filterTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.DAILY -> tasks.filter { it.isDaily }
        }
    }

    private fun calculateTaskEffects(priority: Priority, isOnTime: Boolean): Triple<Int, Int, Int> {
        return when {
            isOnTime -> when (priority) {
                Priority.HIGH -> Triple(15, 20, 50)
                Priority.MEDIUM -> Triple(10, 15, 30)
                Priority.LOW -> Triple(5, 10, 20)
            }
            else -> when (priority) {
                Priority.HIGH -> Triple(-20, -10, 5)
                Priority.MEDIUM -> Triple(-15, -5, 3)
                Priority.LOW -> Triple(-10, 0, 1)
            }
        }
    }

    private fun WizardProfile.checkLevelUp(): WizardProfile {
        var newLevel = level
        var remainingExp = experience
        while (remainingExp >= EXP_PER_LEVEL) {
            remainingExp -= EXP_PER_LEVEL
            newLevel++
        }
        return copy(level = newLevel, experience = remainingExp)
    }
}