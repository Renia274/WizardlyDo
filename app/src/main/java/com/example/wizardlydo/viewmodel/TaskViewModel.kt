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
import com.google.firebase.Timestamp
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
import kotlin.math.ceil

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



    fun createTask(task: Task) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                try {
                    val createdTask = task.copy(userId = userId)
                    taskRepository.insertTask(createdTask)

                    // Store creation time and ID for recent task tracking
                    recentlyCreatedTaskId = createdTask.id
                    lastCreationTime = System.currentTimeMillis()
                    taskRecentlyCreated = true

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
                        isLoading = false,
                        onFilterChange = { filter -> setFilter(filter) }
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

    fun completeTask(taskId: Int, notificationService: TaskNotificationService? = null) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in", isLoading = false) }
                    return@launch
                }

                val task = taskRepository.getTaskById(taskId) ?: run {
                    mutableState.update { it.copy(error = "Task not found", isLoading = false) }
                    return@launch
                }

                if (task.isCompleted) {
                    mutableState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull() ?: run {
                    mutableState.update { it.copy(error = "Wizard profile not found", isLoading = false) }
                    return@launch
                }

                // Calculate rewards
                val (hpGain, staminaGain, expGain) = calculateTaskEffects(
                    priority = task.priority,
                    isOnTime = task.dueDate?.let { System.currentTimeMillis() <= it } ?: true,
                    currentLevel = wizardProfile.level
                )

                // Calculate new stats
                var newHealth = wizardProfile.health + hpGain
                var newStamina = wizardProfile.stamina + staminaGain
                var newExperience = wizardProfile.experience + expGain
                var newLevel = wizardProfile.level
                var newMaxHealth = wizardProfile.maxHealth

                // Check for level up
                if (newExperience >= EXP_PER_LEVEL) {
                    newLevel++
                    newExperience -= EXP_PER_LEVEL
                    newMaxHealth = (wizardProfile.maxHealth + 10).coerceAtMost(150)
                    newHealth = newMaxHealth // Full heal on level up
                    newStamina = (wizardProfile.stamina + 5).coerceAtMost(100)
                } else {
                    // Cap stats if not leveling up
                    newHealth = newHealth.coerceAtMost(wizardProfile.maxHealth)
                    newStamina = newStamina.coerceAtMost(100)
                }

                // Update profile
                val updatedProfile = wizardProfile.copy(
                    level = newLevel,
                    health = newHealth,
                    maxHealth = newMaxHealth,
                    stamina = newStamina,
                    experience = newExperience,
                    totalTasksCompleted = wizardProfile.totalTasksCompleted + 1,
                    consecutiveTasksCompleted = wizardProfile.consecutiveTasksCompleted + 1,
                    lastTaskCompleted = Timestamp.now()
                )

                // Save updates
                wizardRepository.updateWizardProfile(userId, updatedProfile)
                taskRepository.updateTaskCompletionStatus(taskId, true)

                // Force UI update by reloading data
                loadData()

                // Show notification
                notificationService?.showTaskCompletionNotification(
                    task = task,
                    wizardProfile = updatedProfile,
                    hpGained = hpGain
                )

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

    fun calculateTaskEffects(
        priority: Priority,
        isOnTime: Boolean,
        currentLevel: Int
    ): Triple<Int, Int, Int> {
        // Base values
        val baseHp = 10
        val baseStamina = 15
        val baseXp = 50

        // Level scaling
        val scale = when {
            currentLevel < 5 -> 1.0f
            currentLevel < 8 -> 0.8f
            else -> 0.6f
        }

        return when {
            isOnTime -> when (priority) {
                Priority.HIGH -> Triple(
                    (baseHp * 1.5 * scale).toInt(),
                    (baseStamina * 1.3 * scale).toInt(),
                    (baseXp * 1.5 * scale).toInt()
                )
                Priority.MEDIUM -> Triple(
                    (baseHp * scale).toInt(),
                    (baseStamina * scale).toInt(),
                    (baseXp * scale).toInt()
                )
                Priority.LOW -> Triple(
                    (baseHp * 0.5 * scale).toInt(),
                    (baseStamina * 0.7 * scale).toInt(),
                    (baseXp * 0.7 * scale).toInt()
                )
            }
            else -> when (priority) { // Overdue penalties
                Priority.HIGH -> Triple(-20, -10, 5)
                Priority.MEDIUM -> Triple(-15, -5, 3)
                Priority.LOW -> Triple(-10, 0, 1)
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




    fun getTasksToNextLevel(wizardProfile: WizardProfile?): Int {
        wizardProfile ?: return 0

        val expPerLevel = EXP_PER_LEVEL
        val expToNextLevel = expPerLevel - wizardProfile.experience

        // Calculate tasks needed based on level-specific experience per task
        return when {
            wizardProfile.level < 5 -> {
                // Levels 1-4: 4 tasks per level (250 XP per task)
                val expPerTask = 250
                ceil(expToNextLevel / expPerTask.toFloat()).toInt().coerceAtLeast(1)
            }
            wizardProfile.level < 8 -> {
                // Levels 5-7: 6 tasks per level (~167 XP per task)
                val expPerTask = 167
                ceil(expToNextLevel / expPerTask.toFloat()).toInt().coerceAtLeast(1)
            }
            else -> {
                // Levels 8+: 10 tasks per level (100 XP per task)
                val expPerTask = 100
                ceil(expToNextLevel / expPerTask.toFloat()).toInt().coerceAtLeast(1)
            }
        }
    }
//    // Calculate health based on completed tasks
//    fun calculateHealthFromTasks(baseHealth: Int, completedTaskCount: Int, level: Int): Int {
//        // Each task gives HP based on level
//        val hpPerTask = when {
//            level < 5 -> 10  // Levels 1-4: 10 HP per task
//            level < 8 -> 8   // Levels 5-7: 8 HP per task
//            else -> 5        // Levels 8+: 5 HP per task
//        }
//
//        val hpFromTasks = completedTaskCount * hpPerTask
//        return (baseHealth + hpFromTasks).coerceAtMost(MAX_WIZARD_HEALTH)
//    }
//
//    // Calculate stamina based on completed tasks
//    fun calculateStaminaFromTasks(baseStamina: Int, completedTaskCount: Int, level: Int): Int {
//        // Each task gives 5 stamina
//        val staminaPerTask = 5
//        val staminaFromTasks = completedTaskCount * staminaPerTask
//        return (baseStamina + staminaFromTasks).coerceIn(0, 100)
//    }


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


}