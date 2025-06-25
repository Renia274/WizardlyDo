package com.wizardlydo.app.viewmodel.tasks

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.SoundPool
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wizardlydo.app.R
import com.wizardlydo.app.data.models.EditTaskField
import com.wizardlydo.app.data.models.EditTaskState
import com.wizardlydo.app.data.models.TaskFilter
import com.wizardlydo.app.data.models.TaskUiState
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.repository.tasks.TaskRepository
import com.wizardlydo.app.repository.wizard.WizardRepository
import com.wizardlydo.app.room.WizardDatabase
import com.wizardlydo.app.utilities.TaskNotificationService
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
    private val context: Context,
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

    private val soundPool: SoundPool by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private val level30SoundId: Int by lazy {
        soundPool.load(context, R.raw.level_30_achievement, 1)
    }

    companion object {
        private const val EXP_PER_LEVEL = 1000
        private const val MAX_LEVEL = 30
        private const val BASE_XP_GAIN = 25
        private const val BASE_HP_GAIN = 5
        private const val BASE_STAMINA_GAIN = 3
    }


    /**
     * Calculate XP needed for next task progress milestone
     */
    fun getXPNeededForNextTaskProgress(profile: WizardProfile): Triple<Int, Int, Int> {
        val currentLevel = profile.level
        val currentExperience = profile.experience

        // Get tasks required for current level
        val tasksRequiredForLevel = getTasksRequiredForLevel(currentLevel)

        // Calculate XP per task for current level
        val expPerTask = if (tasksRequiredForLevel > 0) EXP_PER_LEVEL / tasksRequiredForLevel else 0

        // Calculate current tasks completed
        val currentTasksCompleted = if (expPerTask > 0) {
            (currentExperience / expPerTask).coerceAtMost(tasksRequiredForLevel)
        } else {
            0
        }

        // Calculate XP needed for next task milestone
        val nextTaskMilestone = currentTasksCompleted + 1
        val xpNeededForNextTask = if (nextTaskMilestone <= tasksRequiredForLevel) {
            (nextTaskMilestone * expPerTask) - currentExperience
        } else {
            0
        }

        return Triple(xpNeededForNextTask, nextTaskMilestone, tasksRequiredForLevel)
    }

    /**
     * Show toast message about XP progress
     */
    fun showXPProgressToast(profile: WizardProfile) {
        val (xpNeeded, nextMilestone, totalTasks) = getXPNeededForNextTaskProgress(profile)
        val currentTasks = getXPBasedTaskProgress(profile).first

        val message = when {
            currentTasks >= totalTasks -> {
                "Complete all ${totalTasks} tasks to level up!"
            }
            xpNeeded <= 0 -> {
                "You've reached ${currentTasks}/${totalTasks} tasks for this level!"
            }
            else -> {
                "You need ${xpNeeded} more XP to reach ${nextMilestone}/${totalTasks} tasks"
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show detailed XP breakdown toast
     */
    fun showDetailedXPToast(profile: WizardProfile) {
        val currentLevel = profile.level
        val currentExperience = profile.experience
        val tasksRequired = getTasksRequiredForLevel(currentLevel)
        val expPerTask = if (tasksRequired > 0) EXP_PER_LEVEL / tasksRequired else 0
        val currentTasks = if (expPerTask > 0) (currentExperience / expPerTask).coerceAtMost(tasksRequired) else 0
        val (xpNeeded, nextMilestone, totalTasks) = getXPNeededForNextTaskProgress(profile)

        val message = buildString {
            append("Level $currentLevel Progress:\n")
            append("Current: ${currentTasks}/${totalTasks} tasks (${currentExperience}/${EXP_PER_LEVEL} XP)\n")
            append("Each task gives ~${expPerTask} XP\n")
            if (xpNeeded > 0 && currentTasks < totalTasks) {
                append("Need ${xpNeeded} XP for ${nextMilestone}/${totalTasks}")
            } else {
                append("Ready to level up!")
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    private  fun clearDatabaseOnce() {
        val sharedPrefs = context.getSharedPreferences("wizard_app_prefs", Context.MODE_PRIVATE)
        val hasCleared = sharedPrefs.getBoolean("database_cleared_v1", false)

        if (!hasCleared) {
            try {
                val database = WizardDatabase.getDatabase(context)
                database.clearAllTables()

                sharedPrefs.edit {
                    putBoolean("database_cleared_v1", true)
                }

                Log.d("TaskViewModel", "Database cleared once successfully")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to clear database: ${e.message}")
            }
        }
    }

    init {
        viewModelScope.launch {
            clearDatabaseOnce()
            loadData()
        }
    }

    fun setNotificationService(service: TaskNotificationService) {
        taskNotificationService = service
    }

    private fun checkForLevel30Achievement(newLevel: Int) {
        if (newLevel == 30) {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone?.play()

            mutableState.update { it.copy(showLevel30Dialog = true) }
        }
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

        currentPage.value = 1
        updateFilteredTasks()
    }

    fun resetSearchFilters() {
        searchQuery.value = ""
        selectedPriority.value = null
        taskType.value = TaskFilter.ALL

        currentPage.value = 1
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
        if (!searchActive.value) {
            return tasks
        }

        var filteredTasks = tasks

        if (searchQuery.value.isNotBlank()) {
            val searchTerms = searchQuery.value.lowercase().trim()
            filteredTasks = filteredTasks.filter { task ->
                task.title.lowercase().contains(searchTerms) ||
                        task.description.lowercase().contains(searchTerms) ||
                        task.category?.lowercase()?.contains(searchTerms) == true
            }
        }

        selectedPriority.value?.let { priority ->
            filteredTasks = filteredTasks.filter { it.priority == priority }
        }

        filteredTasks = when (taskType.value) {
            TaskFilter.ALL -> filteredTasks
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

                    recentlyCreatedTaskId = createdTask.id
                    lastCreationTime = System.currentTimeMillis()
                    taskRecentlyCreated = true

                    mutableState.update {
                        it.copy(recentlyCreatedTask = createdTask)
                    }

                    loadData()
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

                val tasks = try {
                    taskRepository.getAllTasks(userId)
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to get tasks: ${e.message}")
                    mutableState.update { it.copy(error = "Failed to load tasks: ${e.message}") }
                    emptyList()
                }

                allTasks = tasks

                val searchFiltered = applySearchFiltersToTasks(tasks)
                val currentFilterTasks = filterTasks(searchFiltered, uiState.value.currentFilter)

                val totalPages = (currentFilterTasks.size + pageSize - 1) / pageSize
                val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

                val start = (currentPageValue - 1) * pageSize
                val end = minOf(start + pageSize, currentFilterTasks.size)

                val paginatedTasks = if (currentFilterTasks.isEmpty()) {
                    emptyList()
                } else {
                    currentFilterTasks.subList(start, end)
                }

                val currentProfile = mutableState.value.wizardProfile?.getOrNull()

                val profileToUse = if (wizardProfile != null) {
                    if (wizardProfile.outfit.isBlank() && currentProfile?.outfit?.isNotBlank() == true) {
                        wizardProfile.copy(outfit = currentProfile.outfit)
                    } else {
                        wizardProfile
                    }
                } else {
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

    fun hideLevel30Dialog() {
        mutableState.update { it.copy(showLevel30Dialog = false) }
    }

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

                val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull() ?: run {
                    mutableState.update { it.copy(error = "Wizard profile not found") }
                    return@launch
                }

                val (hpGain, staminaGain, expGain) = calculateTaskEffects(
                    priority = task.priority,
                    isOnTime = task.dueDate?.let { System.currentTimeMillis() <= it } ?: true,
                    currentLevel = wizardProfile.level,
                    currentStamina = wizardProfile.stamina
                )

                var newHealth = wizardProfile.health + hpGain
                var newStamina = wizardProfile.stamina + staminaGain
                var newExperience = wizardProfile.experience + expGain
                var newLevel = wizardProfile.level
                var newMaxHealth = wizardProfile.maxHealth
                var newMaxStamina = wizardProfile.maxStamina

                val actualHpGain: Int
                val actualStaminaGain: Int

                val isLevelUp = newExperience >= EXP_PER_LEVEL
                if (isLevelUp && newLevel < MAX_LEVEL) {
                    newLevel++
                    newExperience -= EXP_PER_LEVEL

                    newMaxHealth = WizardProfile.calculateMaxHealth(newLevel)
                    newMaxStamina = WizardProfile.calculateMaxStamina(newLevel)

                    actualHpGain = newMaxHealth - wizardProfile.health
                    newHealth = newMaxHealth

                    actualStaminaGain = 10
                    newStamina = (wizardProfile.stamina + 10).coerceAtMost(newMaxStamina)

                    if (newLevel == 30) {
                        checkForLevel30Achievement(newLevel)
                    }
                } else {
                    newHealth = newHealth.coerceAtMost(wizardProfile.maxHealth)
                    newStamina = newStamina.coerceAtMost(wizardProfile.maxStamina)

                    actualHpGain = hpGain
                    actualStaminaGain = staminaGain
                }

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

                taskRepository.updateTaskCompletionStatus(taskId, true)

                wizardRepository.updateWizardProfile(userId, updatedProfile)

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

    override fun onCleared() {
        super.onCleared()
        soundPool.release()
    }

    private suspend fun loadTasksOnly(userId: String) {
        try {
            val tasks = taskRepository.getAllTasks(userId)

            allTasks = tasks

            val searchFiltered = applySearchFiltersToTasks(tasks)
            val currentFilterTasks = filterTasks(searchFiltered, uiState.value.currentFilter)

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
        val levelFactor = (currentLevel / 5f).coerceAtLeast(1f)

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
            else -> when (priority) {
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
                            isLoading = false
                        )
                    }
                } else {
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
                )

                taskRepository.updateTask(updatedTask)
                loadData()
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
                    loadData()
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
        currentPage.value = 1
        mutableState.update { state ->
            val searchFiltered = applySearchFiltersToTasks(allTasks)
            val filteredTasks = filterTasks(searchFiltered, filter)

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

    private fun filterTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    /**
     * Calculate XP-based task progress for current level
     */
    fun getXPBasedTaskProgress(profile: WizardProfile): Pair<Int, Int> {
        val currentLevel = profile.level
        val currentExperience = profile.experience

        val tasksRequiredForLevel = getTasksRequiredForLevel(currentLevel)
        val expPerTask = if (tasksRequiredForLevel > 0) EXP_PER_LEVEL / tasksRequiredForLevel else 0

        val tasksCompleted = if (expPerTask > 0) {
            (currentExperience / expPerTask).coerceAtMost(tasksRequiredForLevel)
        } else {
            0
        }

        return Pair(tasksCompleted, tasksRequiredForLevel)
    }

    /**
     * Get tasks required for specific level
     */
    fun getTasksRequiredForLevel(level: Int): Int {
        return when (level) {
            in 1..4 -> 10
            in 5..8 -> 15
            in 9..14 -> 20
            in 15..19 -> 25
            in 20..24 -> 30
            in 25..29 -> 35
            else -> 40
        }
    }

    fun getTasksCompletedForLevel(profile: WizardProfile): Int {
        val currentLevelExp = profile.experience
        val expPerTaskForLevel = calculateExpPerTask(profile.level)
        val tasksCompleted = if (expPerTaskForLevel > 0) {
            currentLevelExp / expPerTaskForLevel
        } else {
            0
        }
        val totalTasksForLevel = getTasksRequiredForLevel(profile.level)

        return tasksCompleted.coerceAtMost(totalTasksForLevel).coerceAtLeast(0)
    }

    private fun calculateExpPerTask(level: Int): Int {
        val tasksRequired = getTasksRequiredForLevel(level)
        return if (tasksRequired > 0) EXP_PER_LEVEL / tasksRequired else 0
    }

    /**
     * Check if wizard is currently defeated (health <= 0)
     */
    fun isWizardDefeated(): Boolean {
        return (mutableState.value.wizardProfile?.getOrNull()?.health ?: 1) <= 0
    }

    /**
     * Get number of tasks completed toward revival
     */
    fun getRevivalProgress(): Pair<Int, Int> {
        val tasksCompleted = mutableState.value.wizardProfile?.getOrNull()?.consecutiveTasksCompleted ?: 0
        return Pair(tasksCompleted, 3)
    }

    /**
     * Calculate health to be restored on revival (30% of max)
     */
    fun calculateRevivalHealth(): Int {
        val wizardProfile = mutableState.value.wizardProfile?.getOrNull() ?: return 1
        return (wizardProfile.maxHealth * 0.3).toInt().coerceAtLeast(1)
    }

    /**
     * Update the wizard profile after completing a task toward revival
     */
    fun updateRevivalProgress(
        taskId: Int,
        onRevived: ((restoredHealth: Int) -> Unit)? = null
    ) {
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

                taskRepository.updateTaskCompletionStatus(taskId, true)

                val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull() ?: run {
                    mutableState.update { it.copy(error = "Wizard profile not found") }
                    return@launch
                }

                val newConsecutiveTasks = wizardProfile.consecutiveTasksCompleted + 1
                val newTotalTasksCompleted = wizardProfile.totalTasksCompleted + 1
                val tasksNeededForRevival = 3

                if (newConsecutiveTasks >= tasksNeededForRevival) {
                    val restoredHealth = calculateRevivalHealth()

                    val updatedProfile = wizardProfile.copy(
                        health = restoredHealth,
                        consecutiveTasksCompleted = 0,
                        totalTasksCompleted = newTotalTasksCompleted,
                        lastTaskCompleted = Timestamp.now()
                    )

                    wizardRepository.updateWizardProfile(userId, updatedProfile)

                    mutableState.update { state ->
                        state.copy(
                            wizardProfile = Result.success(updatedProfile),
                            isLoading = false
                        )
                    }

                    onRevived?.invoke(restoredHealth)
                } else {
                    val updatedProfile = wizardProfile.copy(
                        consecutiveTasksCompleted = newConsecutiveTasks,
                        totalTasksCompleted = newTotalTasksCompleted,
                        lastTaskCompleted = Timestamp.now()
                    )

                    wizardRepository.updateWizardProfile(userId, updatedProfile)

                    mutableState.update { state ->
                        state.copy(
                            wizardProfile = Result.success(updatedProfile),
                            isLoading = false
                        )
                    }
                }

                loadTasksOnly(userId)

            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Revival progress update failed: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}