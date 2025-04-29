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
import com.example.wizardlydo.utilities.TaskNotificationService
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

    // Task notification service might be needed
    private var taskNotificationService: TaskNotificationService? = null

    companion object {
        private const val EXP_PER_LEVEL = 1000
        private const val MAX_WIZARD_HEALTH = 150 // Maximum health cap
    }

    init {
        loadData()
    }

    fun setNotificationService(service: TaskNotificationService) {
        taskNotificationService = service
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
            } ?: run {
                mutableState.update {
                    it.copy(error = "User ID not available. Please sign in again.")
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
            val currentTask = currentState.task ?: run {
                mutableEditTaskState.update { it.copy(error = "No task to save") }
                return@launch
            }

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

    // Updated to handle HP with the cap of 150 and new notification, with proper null checks
    fun completeTask(taskId: Int, notificationService: TaskNotificationService? = null) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            try {
                // Step 1: Get the user ID with null check
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in", isLoading = false) }
                    return@launch
                }

                // Step 2: Get the task with null check
                val task = taskRepository.getTaskById(taskId) ?: run {
                    mutableState.update { it.copy(error = "Task not found", isLoading = false) }
                    return@launch
                }

                // Step 3: Get the wizard profile with null check
                val wizardProfileResult = wizardRepository.getWizardProfile(userId)
                val wizardProfile = wizardProfileResult.getOrNull() ?: run {
                    mutableState.update {
                        it.copy(
                            error = "Failed to load wizard profile: ${wizardProfileResult.exceptionOrNull()?.message ?: "Unknown error"}",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Step 4: Calculate completion effects
                val now = System.currentTimeMillis()
                val isOnTime = task.dueDate?.let { dueDate -> now <= dueDate } ?: true

                val (hpChange, staminaChange, expGain) = calculateTaskEffects(
                    priority = task.priority,
                    isOnTime = isOnTime,
                    currentLevel = wizardProfile.level
                )

                // Step 5: Apply HP cap of 150 and calculate actual HP gained
                val newHealth = (wizardProfile.health + hpChange).coerceIn(0, MAX_WIZARD_HEALTH)
                val hpGained = newHealth - wizardProfile.health

                // Step 6: Update wizard profile with null check for level up
                val updatedProfile = wizardProfile.copy(
                    health = newHealth,
                    stamina = (wizardProfile.stamina + staminaChange).coerceIn(0, 100),
                    experience = wizardProfile.experience + expGain
                ).checkLevelUp()

                // Step 7: Save updates to repositories
                val updateProfileResult = wizardRepository.updateWizardProfile(userId, updatedProfile)
                if (!updateProfileResult.isSuccess) {
                    mutableState.update {
                        it.copy(
                            error = "Failed to update profile: ${updateProfileResult.exceptionOrNull()?.message ?: "Unknown error"}",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Update task completion status
                try {
                    taskRepository.updateTaskCompletionStatus(taskId, true)
                } catch (e: Exception) {
                    mutableState.update {
                        it.copy(error = "Failed to mark task as completed: ${e.message}", isLoading = false)
                    }
                    return@launch
                }

                // Step 8: Show completion notification with HP gained
                // Use the passed notification service or fall back to stored one
                val service = notificationService ?: taskNotificationService
                service?.showTaskCompletionNotification(
                    task = task,
                    wizardProfile = updatedProfile,
                    hpGained = hpGained
                )

                // Step 9: Refresh data and update UI state
                loadData()
                mutableState.update { it.copy(isLoading = false) }

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

    // Updated to consider level progression in HP calculation
    private fun calculateTaskEffects(priority: Priority, isOnTime: Boolean, currentLevel: Int): Triple<Int, Int, Int> {
        // Base HP gain is 10, which gets modified by priority and timing
        val baseHpGain = 10

        // Calculate adjusted HP based on the level
        val hpMultiplier = when {
            currentLevel < 5 -> 1.0  // Levels 1-4: normal gain
            currentLevel < 8 -> 0.83 // Levels 5-7: slightly reduced (need 6 tasks)
            else -> 0.5             // Levels 8+: half gain (need 10 tasks)
        }

        return when {
            isOnTime -> when (priority) {
                Priority.HIGH -> Triple(
                    (baseHpGain * 1.5 * hpMultiplier).toInt(), // 15 HP for high priority
                    20,
                    50
                )
                Priority.MEDIUM -> Triple(
                    (baseHpGain * hpMultiplier).toInt(), // 10 HP for medium priority
                    15,
                    30
                )
                Priority.LOW -> Triple(
                    (baseHpGain * 0.5 * hpMultiplier).toInt(), // 5 HP for low priority
                    10,
                    20
                )
            }
            else -> when (priority) {
                Priority.HIGH -> Triple(-20, -10, 5) // Overdue tasks cause damage
                Priority.MEDIUM -> Triple(-15, -5, 3)
                Priority.LOW -> Triple(-10, 0, 1)
            }
        }
    }

    private fun WizardProfile.checkLevelUp(): WizardProfile {
        var newLevel = level
        var remainingExp = experience
        var newMaxHealth = maxHealth
        var newHealth = health

        while (remainingExp >= EXP_PER_LEVEL) {
            remainingExp -= EXP_PER_LEVEL
            newLevel++

            // When leveling up, increase maxHealth if not at cap yet
            if (newMaxHealth < MAX_WIZARD_HEALTH) {
                // Don't exceed the maximum health cap
                newMaxHealth = (newMaxHealth + 10).coerceAtMost(MAX_WIZARD_HEALTH)
                // Full heal on level up
                newHealth = newMaxHealth
            }
        }

        return copy(
            level = newLevel,
            experience = remainingExp,
            maxHealth = newMaxHealth,
            health = newHealth
        )
    }

    // Add this method to your TaskViewModel class
    fun loadUpcomingTasks(onResult: (List<Task>) -> Unit) {
        viewModelScope.launch {
            try {
                val tasks = getUpcomingTasksSync()
                onResult(tasks)
            } catch (e: Exception) {
                mutableState.update { it.copy(error = "Failed to load upcoming tasks: ${e.message}") }
                onResult(emptyList())
            }
        }
    }

    // Function to get upcoming tasks synchronously (for use with notification button)
    suspend fun getUpcomingTasksSync(): List<Task> {
        return try {
            val userId = wizardRepository.getCurrentUserId() ?: return emptyList()

            val currentTime = System.currentTimeMillis()
            // Get tasks due in the next 3 days
            val threeDaysFromNow = currentTime + (3 * 24 * 60 * 60 * 1000)

            // Fetch upcoming tasks from repository
            val upcomingTasks = taskRepository.getUpcomingTasks(userId, currentTime, threeDaysFromNow)

            // Also get overdue tasks
            val overdueTasks = taskRepository.getDueTasks(userId)

            // Combine the lists (overdue first, then upcoming)
            (overdueTasks + upcomingTasks).filter { !it.isCompleted }
        } catch (e: Exception) {
            mutableState.update { it.copy(error = "Failed to load upcoming tasks: ${e.message}") }
            emptyList()
        }
    }

    // Calculate tasks needed to reach next level based on current level
    fun getTasksToNextLevel(wizardProfile: WizardProfile?): Int {
        wizardProfile ?: return 0

        val expPerLevel = EXP_PER_LEVEL
        val expToNextLevel = expPerLevel - wizardProfile.experience

        return when {
            wizardProfile.level < 5 -> (expToNextLevel / 250).coerceAtLeast(1) // 4 tasks (250 exp per task)
            wizardProfile.level < 8 -> (expToNextLevel / 167).coerceAtLeast(1) // 6 tasks (167 exp per task)
            else -> (expToNextLevel / 100).coerceAtLeast(1) // 10 tasks (100 exp per task)
        }
    }

    // Get total tasks needed for current level
    fun getTotalTasksForLevel(level: Int): Int {
        return when {
            level < 5 -> 4
            level < 8 -> 6
            else -> 10
        }
    }
}