package com.example.wizardlydo.viewmodel.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.models.EditTaskField
import com.example.wizardlydo.data.models.EditTaskState
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task
import com.example.wizardlydo.data.wizard.WizardProfile
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

    private val searchQuery = MutableStateFlow("")
    private val selectedPriority = MutableStateFlow<Priority?>(null)
    private val taskType = MutableStateFlow(TaskFilter.ALL)
    private val searchActive = MutableStateFlow(false)

    private var taskRecentlyCreated = false
    private var recentlyCreatedTaskId: Int? = null
    private var lastCreationTime = 0L

    private var taskNotificationService: TaskNotificationService? = null

    private val pageSize = 10
    private val currentPage = MutableStateFlow(1)
    val currentPageState = currentPage.asStateFlow()

    private var allTasks = listOf<Task>()

    companion object {
        private const val EXP_PER_LEVEL = 1000
        private const val MAX_LEVEL = 30
        private const val BASE_XP_GAIN = 25
        private const val BASE_HP_GAIN = 5
        private const val BASE_STAMINA_GAIN = 3
    }

    init {
        loadData()
    }

    fun setNotificationService(service: TaskNotificationService) {
        taskNotificationService = service
    }

    fun nextPage() {
        currentPage.value++
        updateFilteredTasks()
    }

    fun previousPage() {
        if (currentPage.value > 1) {
            currentPage.value--
            updateFilteredTasks()
        }
    }

    fun activateSearch() {
        searchActive.value = true
        updateState()
    }

    fun deactivateSearch() {
        searchActive.value = false
        resetSearchFilters()
        updateState()
    }

    fun applySearchFilters(query: String, priority: Priority?, type: TaskFilter) {
        searchQuery.value = query
        selectedPriority.value = priority
        taskType.value = type
        searchActive.value = true

        currentPage.value = 1 // Reset to first page when applying new filters
        updateFilteredTasks()
    }

    fun resetSearchFilters() {
        searchQuery.value = ""
        selectedPriority.value = null
        taskType.value = TaskFilter.ALL

        currentPage.value = 1 // Reset to first page
        updateFilteredTasks()
    }

    private fun updateState() {
        mutableState.update { state ->
            state.copy(
                searchActive = searchActive.value,
                searchQuery = searchQuery.value,
                selectedPriority = selectedPriority.value,
                taskType = taskType.value
            )
        }
    }

    private fun updateFilteredTasks() {
        val searchFilters = applySearchFiltersToTasks(allTasks)
        val currentFilterTasks = filterTasks(searchFilters, uiState.value.currentFilter)

        // Apply pagination
        val totalPages = (currentFilterTasks.size + pageSize - 1) / pageSize
        val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

        val start = (currentPageValue - 1) * pageSize
        val end = minOf(start + pageSize, currentFilterTasks.size)

        val paginatedTasks = if (currentFilterTasks.isEmpty()) {
            emptyList()
        } else {
            currentFilterTasks.subList(start, end)
        }

        mutableState.update { it.copy(
            filteredTasks = paginatedTasks,
            totalPages = totalPages,
            currentPage = currentPageValue,
            searchActive = searchActive.value,
            searchQuery = searchQuery.value,
            selectedPriority = selectedPriority.value,
            taskType = taskType.value
        ) }
    }

    private fun applySearchFiltersToTasks(tasks: List<Task>): List<Task> {
        // If search is not active, return all tasks
        if (!searchActive.value) {
            return tasks
        }

        var filteredTasks = tasks

        // Apply text search if not empty
        if (searchQuery.value.isNotBlank()) {
            val searchTerms = searchQuery.value.lowercase().trim()
            filteredTasks = filteredTasks.filter { task ->
                task.title.lowercase().contains(searchTerms) ||
                        task.description.lowercase().contains(searchTerms) ||
                        task.category?.lowercase()?.contains(searchTerms) == true
            }
        }

        // Apply priority filter if selected
        selectedPriority.value?.let { priority ->
            filteredTasks = filteredTasks.filter { it.priority == priority }
        }

        // Apply task type filter

        filteredTasks = when (taskType.value) {
            TaskFilter.ALL -> filteredTasks
            TaskFilter.DAILY -> filteredTasks.filter { it.isDaily }
            TaskFilter.ACTIVE -> filteredTasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> filteredTasks.filter { it.isCompleted }
        }

        return filteredTasks
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

                val wizardProfile = try {
                    wizardRepository.getWizardProfile(userId).getOrThrow()
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to get wizard profile: ${e.message}")
                    mutableState.update {
                        it.copy(
                            error = "Failed to load wizard profile: ${e.message}",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Log the loaded profile details including outfit
                Log.d("TaskViewModel", "Loaded profile - Level: ${wizardProfile?.level}, " +
                        "Health: ${wizardProfile?.health}/${wizardProfile?.maxHealth}, " +
                        "Stamina: ${wizardProfile?.stamina}/${wizardProfile?.maxStamina}, " +
                        "XP: ${wizardProfile?.experience}, " +
                        "Tasks completed: ${wizardProfile?.totalTasksCompleted}, " +
                        "Outfit: '${wizardProfile?.outfit}', " +
                        "Gender: ${wizardProfile?.gender}")

                val tasks = try {
                    taskRepository.getAllTasks(userId)
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to get tasks: ${e.message}")
                    mutableState.update { it.copy(error = "Failed to load tasks: ${e.message}") }
                    emptyList()
                }

                // Store all tasks for filtering
                allTasks = tasks

                // Apply search filters if active, otherwise use regular filtering
                val searchFiltered = applySearchFiltersToTasks(tasks)
                val currentFilterTasks = filterTasks(searchFiltered, uiState.value.currentFilter)

                // Calculate pagination info
                val totalPages = (currentFilterTasks.size + pageSize - 1) / pageSize
                val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

                val start = (currentPageValue - 1) * pageSize
                val end = minOf(start + pageSize, currentFilterTasks.size)

                val paginatedTasks = if (currentFilterTasks.isEmpty()) {
                    emptyList()
                } else {
                    currentFilterTasks.subList(start, end)
                }

                // Preserve the current wizardProfile if already loaded, to avoid losing custom outfit info
                val currentProfile = mutableState.value.wizardProfile?.getOrNull()

                // Only update if we got a new profile AND it has valid outfit information
                val profileToUse = if (wizardProfile != null) {
                    // Check if we need to preserve outfit from existing profile
                    if (wizardProfile.outfit.isBlank() && currentProfile?.outfit?.isNotBlank() == true) {
                        Log.d("TaskViewModel", "Preserving existing outfit: '${currentProfile.outfit}' instead of blank outfit")
                        wizardProfile.copy(outfit = currentProfile.outfit)
                    } else {
                        // Use the newly loaded profile as is
                        Log.d("TaskViewModel", "Using newly loaded profile with outfit: '${wizardProfile.outfit}'")
                        wizardProfile
                    }
                } else {
                    // If wizard profile not loaded , preserve the current profile
                    currentProfile
                }

                mutableState.update {
                    it.copy(
                        wizardProfile = if (profileToUse != null) Result.success(profileToUse) else it.wizardProfile,
                        tasks = tasks,
                        filteredTasks = paginatedTasks,
                        totalPages = totalPages,
                        currentPage = currentPageValue,
                        isLoading = false,
                        onFilterChange = { filter -> setFilter(filter) },
                        searchActive = searchActive.value,
                        searchQuery = searchQuery.value,
                        selectedPriority = selectedPriority.value,
                        taskType = taskType.value
                    )
                }




            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Failed to load: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // handle HP and stamina changes after completion
    fun completeTask(taskId: Int, notificationService: TaskNotificationService? = null) {
        viewModelScope.launch {
            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in") }
                    return@launch
                }

                val task = taskRepository.getTaskById(taskId) ?: run {
                    mutableState.update { it.copy(error = "Task not found") }
                    return@launch
                }

                if (task.isCompleted) {
                    return@launch
                }

                // Get latest profile
                val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull() ?: run {
                    mutableState.update { it.copy(error = "Wizard profile not found") }
                    return@launch
                }

                // Calculate rewards based on priority and on-time status
                val (hpGain, staminaGain, expGain) = calculateTaskEffects(
                    priority = task.priority,
                    isOnTime = task.dueDate?.let { System.currentTimeMillis() <= it } ?: true,
                    currentLevel = wizardProfile.level,
                    currentStamina = wizardProfile.stamina
                )



                // Calculate new stats
                var newHealth = wizardProfile.health + hpGain
                var newStamina = wizardProfile.stamina + staminaGain
                var newExperience = wizardProfile.experience + expGain
                var newLevel = wizardProfile.level
                var newMaxHealth = wizardProfile.maxHealth
                var newMaxStamina = wizardProfile.maxStamina

                // Store original values for notification
                val actualHpGain: Int
                val actualStaminaGain: Int

                // Check for level up
                val isLevelUp = newExperience >= EXP_PER_LEVEL
                if (isLevelUp && newLevel < MAX_LEVEL) {
                    // Level up
                    newLevel++
                    newExperience -= EXP_PER_LEVEL

                    // Increase max health when leveling up
                    newMaxHealth = WizardProfile.calculateMaxHealth(newLevel)
                    newMaxStamina = WizardProfile.calculateMaxStamina(newLevel)

                    // Set health to the new maximum on level up
                    actualHpGain = newMaxHealth - wizardProfile.health
                    newHealth = newMaxHealth

                    // Increase stamina by 10 on level up (but don't fully restore)
                    actualStaminaGain = 10
                    newStamina = (wizardProfile.stamina + 10).coerceAtMost(newMaxStamina)

                    Log.d("TaskViewModel", "Level up! New level: $newLevel, Health: $newHealth/$newMaxHealth, Stamina: $newStamina")
                } else {
                    // Cap stats if not leveling up
                    newHealth = newHealth.coerceAtMost(wizardProfile.maxHealth)
                    newStamina = newStamina.coerceAtMost(wizardProfile.maxStamina)

                    // Use the actual calculated gains
                    actualHpGain = hpGain
                    actualStaminaGain = staminaGain

                    Log.d("TaskViewModel", "Task completed. Health: $newHealth/$newMaxHealth, Stamina: $newStamina")
                }

                // Increment total tasks completed
                val newTotalTasksCompleted = wizardProfile.totalTasksCompleted + 1


                val updatedProfile = wizardProfile.copy(
                    level = newLevel,
                    health = newHealth,
                    maxHealth = newMaxHealth,
                    stamina = newStamina,
                    maxStamina = newMaxStamina,
                    experience = newExperience,
                    totalTasksCompleted = newTotalTasksCompleted,
                    consecutiveTasksCompleted = 0,
                    lastTaskCompleted = Timestamp.now()
                )



                // Mark task as completed
                taskRepository.updateTaskCompletionStatus(taskId, true)

                // Save profile updates to database

                wizardRepository.updateWizardProfile(userId, updatedProfile)

                // Show notification with HP and stamina gains
                notificationService?.showTaskCompletionNotification(
                    task = task,
                    wizardProfile = updatedProfile,
                    hpGained = actualHpGain,
                    staminaGained = actualStaminaGain
                )

                mutableState.update { currentState ->
                    currentState.copy(
                        wizardProfile = Result.success(updatedProfile),
                        isLoading = false
                    )
                }


                loadTasksOnly(userId)

            } catch (e: Exception) {
                Log.e("TaskViewModel", "Completion failed: ${e.message}", e)
                mutableState.update {
                    it.copy(
                        error = "Completion failed: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadTasksOnly(userId: String) {
        try {
            val tasks = taskRepository.getAllTasks(userId)

            // Update the all tasks list
            allTasks = tasks

            // Apply search filters if active
            val searchFiltered = applySearchFiltersToTasks(tasks)
            val currentFilterTasks = filterTasks(searchFiltered, uiState.value.currentFilter)

            // Apply pagination
            val totalPages = (currentFilterTasks.size + pageSize - 1) / pageSize
            val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

            val start = (currentPageValue - 1) * pageSize
            val end = minOf(start + pageSize, currentFilterTasks.size)

            val paginatedTasks = if (currentFilterTasks.isEmpty()) {
                emptyList()
            } else {
                currentFilterTasks.subList(start, end)
            }

            mutableState.update { state ->
                state.copy(
                    tasks = tasks,
                    filteredTasks = paginatedTasks,
                    totalPages = totalPages,
                    currentPage = currentPageValue
                )
            }
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Failed to load tasks: ${e.message}", e)
        }
    }

    fun calculateTaskEffects(
        priority: Priority,
        isOnTime: Boolean,
        currentLevel: Int,
        currentStamina: Int
    ): Triple<Int, Int, Int> {
        // Progressive HP gain based on level
        val levelFactor = (currentLevel / 5f).coerceAtLeast(1f)

        // Stamina gain scales based on current stamina and XP gained
        val staminaEfficiency = when {
            currentStamina < 30 -> 1.5f
            currentStamina < 50 -> 1.2f
            currentStamina < 70 -> 1.0f
            else -> 0.7f
        }

        return when {
            isOnTime -> when (priority) {
                Priority.HIGH -> Triple(
                    (BASE_HP_GAIN * 1.5 * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * 2 * staminaEfficiency).toInt(),
                    (BASE_XP_GAIN * 1.5).toInt()
                )
                Priority.MEDIUM -> Triple(
                    (BASE_HP_GAIN * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * 1.5 * staminaEfficiency).toInt(),
                    BASE_XP_GAIN
                )
                Priority.LOW -> Triple(
                    (BASE_HP_GAIN * 0.5 * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * staminaEfficiency).toInt(),
                    (BASE_XP_GAIN * 0.7).toInt()
                )
            }
            else -> when (priority) { // Overdue penalties
                Priority.HIGH -> Triple(-15, -5, 3)
                Priority.MEDIUM -> Triple(-10, -3, 2)
                Priority.LOW -> Triple(-5, -1, 1)
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

    fun setFilter(filter: TaskFilter) {
        currentPage.value = 1 // Reset to first page when changing filter
        mutableState.update { state ->
            // Apply search filters if active
            val searchFiltered = applySearchFiltersToTasks(allTasks)
            val filteredTasks = filterTasks(searchFiltered, filter)

            // Apply pagination
            val totalPages = (filteredTasks.size + pageSize - 1) / pageSize
            val start = 0
            val end = minOf(pageSize, filteredTasks.size)

            val paginatedTasks = if (filteredTasks.isEmpty()) {
                emptyList()
            } else {
                filteredTasks.subList(start, end)
            }

            state.copy(
                currentFilter = filter,
                filteredTasks = paginatedTasks,
                totalPages = totalPages,
                currentPage = 1
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

    // show all tasks in ALL tab (both completed and non-completed)
    private fun filterTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> tasks  // Show all tasks regardless of completion status
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.DAILY -> tasks.filter { it.isDaily }
        }
    }

    fun getTasksCompletedForLevel(profile: WizardProfile): Int {
        val currentLevelExp = profile.experience
        val expPerTaskForLevel = calculateExpPerTask(profile.level)
        val tasksCompleted = currentLevelExp / expPerTaskForLevel
        val totalTasksForLevel = getTasksRequiredForLevel(profile.level)

        return tasksCompleted.coerceAtMost(totalTasksForLevel).coerceAtLeast(0)
    }



    fun getTasksRequiredForLevel(level: Int): Int {
        return when (level) {
            in 1..4 -> 10
            in 5..8 -> 15
            in 9..14 -> 20
            in 15..19 -> 25
            in 20..24 -> 30
            in 25..29 -> 35
            else -> 40 // Level 30+
        }
    }

    private fun calculateExpPerTask(level: Int): Int {
        return EXP_PER_LEVEL / getTasksRequiredForLevel(level)
    }

    fun getTaskSetInfo(wizardProfile: WizardProfile): Pair<Int, Int> {
        val tasksCompleted = getTasksCompletedForLevel(wizardProfile)
        val totalTasksForLevel = getTasksRequiredForLevel(wizardProfile.level)
        return Pair(tasksCompleted, totalTasksForLevel)
    }


}