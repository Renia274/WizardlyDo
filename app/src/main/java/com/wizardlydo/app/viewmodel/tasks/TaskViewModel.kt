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
import com.wizardlydo.app.models.EditTaskField
import com.wizardlydo.app.models.EditTaskState
import com.wizardlydo.app.models.TaskUiState
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.repository.tasks.TaskRepository
import com.wizardlydo.app.repository.wizard.WizardRepository
import com.wizardlydo.app.room.WizardDatabase
import com.wizardlydo.app.utilities.TaskNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchActive = MutableStateFlow(false)

    private var taskRecentlyCreated = false
    private var recentlyCreatedTaskId: Int? = null
    private var lastCreationTime = 0L

    private var taskNotificationService: TaskNotificationService? = null

    private val pageSize = 10
    private val currentPage = MutableStateFlow(1)

    private var allTasks = listOf<Task>()

    private val completedTutorialTasks = mutableSetOf<Int>()

    // Flag to prevent profile reload after task completion
    private var profileRecentlyUpdated = false
    private var lastProfileUpdateTime = 0L

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
        private const val BASE_XP_GAIN = 50
        private const val BASE_HP_GAIN = 5
        private const val BASE_STAMINA_GAIN = 3
    }

    /**
     * Calculate task progress for current level based on XP earned.
     *
     * Since tasks give different XP (LOW=35, MEDIUM=50, HIGH=75), we use 50 XP as the
     * baseline to estimate task count. Uses floor division so 1 completed task shows as 1,
     * not 2 (even for HIGH priority). The XP bar shows exact progress; task count gives
     * users a clear goal.
     *
     * Progression is designed to gradually increase difficulty:
     * - Early game (1-4): Learn the system with 20 tasks per level
     * - Mid game (5-14): Build habits with 25-30 tasks per level
     * - Late game (15-24): Challenge yourself with 40-50 tasks per level
     * - End game (25-29): Master level with 60 tasks per level
     * - Final level (30): Ultimate challenge with 100 tasks
     *
     * Total tasks to reach Level 30: ~1,210 tasks (challenging but achievable)
     *
     * Examples:
     * - 75 XP (1 HIGH) → 75÷50 = 1 task shown ✅
     * - 150 XP (2 HIGH) → 150÷50 = 3 tasks shown (bonus progress!)
     * - 410 XP → 410÷50 = 8 tasks shown
     *
     * @return Pair(tasksCompleted, totalTasksNeeded) for current level
     */
    fun getActualTaskProgress(profile: WizardProfile): Pair<Int, Int> {
        val currentLevel = profile.level
        val currentXP = profile.experience  // Resets to 0 after each level up

        Log.d("TaskViewModel", "getActualTaskProgress - Level: $currentLevel, XP: $currentXP")

        // Tasks needed per level set (progressively harder for balanced late-game challenge)
        val tasksPerLevel = when (currentLevel) {
            in 1..4 -> 20      // Levels 1-4: Easy introduction (20 tasks × 4 levels = 80 total)
            in 5..8 -> 25      // Levels 5-8: Building momentum (25 tasks × 4 levels = 100 total)
            in 9..14 -> 30     // Levels 9-14: Mid-game grind (30 tasks × 6 levels = 180 total)
            in 15..19 -> 40    // Levels 15-19: Getting serious (40 tasks × 5 levels = 200 total)
            in 20..24 -> 50    // Levels 20-24: Advanced challenge (50 tasks × 5 levels = 250 total)
            in 25..29 -> 60    // Levels 25-29: Master level (60 tasks × 5 levels = 300 total)
            else -> 100        // Level 30: Ultimate endgame (100 tasks for final achievement!)
        }

        // Convert XP to approximate task count (50 XP = 1 task baseline)
        // Floor division ensures 1 completed task shows as 1, not rounded up
        val tasksCompleted = (currentXP / 50).coerceIn(0, tasksPerLevel)

        Log.d("TaskViewModel", "Result: $tasksCompleted / $tasksPerLevel")

        return Pair(tasksCompleted, tasksPerLevel)
    }

    /**
     * TEMPORARY: Fix XP to match task count
     * This retroactively calculates what XP should be based on completed tasks
     */
    fun fixXPSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = wizardRepository.getCurrentUserId() ?: return@launch
                val wizardProfile =
                    wizardRepository.getWizardProfile(userId).getOrNull() ?: return@launch

                // Calculate what XP SHOULD be based on task count
                val tasksCompleted = wizardProfile.totalTasksCompleted
                val expectedXP = tasksCompleted * 50  // 50 XP per task average

                Log.d(
                    "TaskViewModel",
                    "Fixing XP: $tasksCompleted tasks should give $expectedXP XP"
                )

                // Calculate new level and remaining XP
                var newLevel = 1
                var remainingXP = expectedXP

                while (remainingXP >= 1000 && newLevel < 30) {
                    remainingXP -= 1000
                    newLevel++
                }

                Log.d("TaskViewModel", "New Level: $newLevel, Remaining XP: $remainingXP")

                val fixedProfile = wizardProfile.copy(
                    level = newLevel,
                    experience = remainingXP,
                    maxHealth = WizardProfile.calculateMaxHealth(newLevel),
                    maxStamina = WizardProfile.calculateMaxStamina(newLevel),
                    health = WizardProfile.calculateMaxHealth(newLevel),
                    stamina = WizardProfile.calculateMaxStamina(newLevel)
                )

                wizardRepository.updateWizardProfile(userId, fixedProfile)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "XP synced! $tasksCompleted tasks = Level $newLevel with $remainingXP XP",
                        Toast.LENGTH_LONG
                    ).show()
                    loadData()
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "XP fix failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to fix XP: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /**
     * Update toast to show actual task progress
     */
    fun showTaskProgressToast(profile: WizardProfile) {
        val (tasksCompleted, tasksNeeded) = getActualTaskProgress(profile)
        val currentLevel = profile.level

        val levelRange = when (currentLevel) {
            in 1..4 -> "1-4"
            in 5..8 -> "5-8"
            in 9..14 -> "9-14"
            in 15..19 -> "15-19"
            in 20..24 -> "20-24"
            in 25..29 -> "25-29"
            else -> "30"
        }

        val message = buildString {
            append("Level Set $levelRange Progress:\n")
            append("Tasks completed: $tasksCompleted / $tasksNeeded\n")
            append("Current Level: $currentLevel\n")
            append("Total XP: ${profile.experience} / 1000\n")
            if (tasksCompleted < tasksNeeded) {
                append("\nComplete ${tasksNeeded - tasksCompleted} more tasks to advance!")
            } else {
                append("\nReady for next level set!")
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Calculate XP needed for next task progress milestone
     */
    fun getXPNeededForNextTaskProgress(profile: WizardProfile): Triple<Int, Int, Int> {
        val currentLevel = profile.level
        val currentExperience = profile.experience

        val tasksRequiredForLevel = getTasksRequiredForLevel(currentLevel)
        val expPerTask = if (tasksRequiredForLevel > 0) EXP_PER_LEVEL / tasksRequiredForLevel else 0

        val currentTasksCompleted = if (expPerTask > 0) {
            (currentExperience / expPerTask).coerceAtMost(tasksRequiredForLevel)
        } else {
            0
        }

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
                "Complete all $totalTasks tasks to level up!"
            }

            xpNeeded <= 0 -> {
                "You've reached ${currentTasks}/${totalTasks} tasks for this level!"
            }

            else -> {
                "You need $xpNeeded more XP to reach ${nextMilestone}/${totalTasks} tasks"
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
        val currentTasks =
            if (expPerTask > 0) (currentExperience / expPerTask).coerceAtMost(tasksRequired) else 0
        val (xpNeeded, nextMilestone, totalTasks) = getXPNeededForNextTaskProgress(profile)

        val message = buildString {
            append("Level $currentLevel Progress:\n")
            append("Current: ${currentTasks}/${totalTasks} tasks (${currentExperience}/${EXP_PER_LEVEL} XP)\n")
            append("Each task gives ~${expPerTask} XP\n")
            if (xpNeeded > 0 && currentTasks < totalTasks) {
                append("Need $xpNeeded XP for ${nextMilestone}/${totalTasks}")
            } else {
                append("Ready to level up!")
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun clearDatabaseOnce() {
        val sharedPrefs = context.getSharedPreferences("wizard_app_prefs", Context.MODE_PRIVATE)
        val hasCleared = sharedPrefs.getBoolean("database_cleared_v1", false)

        if (!hasCleared) {
            viewModelScope.launch(Dispatchers.IO) {
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
    }

    init {
        viewModelScope.launch {
            //clearDatabaseOnce()
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

    fun applySearchFilters(query: String, priority: Priority?, category: String?) {
        searchQuery.value = query
        selectedPriority.value = priority
        selectedCategory.value = category
        searchActive.value = true

        currentPage.value = 1
        updateFilteredTasks()
    }

    fun resetSearchFilters() {
        searchQuery.value = ""
        selectedPriority.value = null
        selectedCategory.value = null

        currentPage.value = 1
        updateFilteredTasks()
    }

    private fun updateState() {
        mutableState.update { state ->
            state.copy(
                searchActive = searchActive.value,
                searchQuery = searchQuery.value,
                selectedPriority = selectedPriority.value,
                selectedCategory = selectedCategory.value
            )
        }
    }

    private fun updateFilteredTasks() {
        val filteredTasks = applySearchFiltersToTasks(allTasks)

        val totalPages = (filteredTasks.size + pageSize - 1) / pageSize
        val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

        val start = (currentPageValue - 1) * pageSize
        val end = minOf(start + pageSize, filteredTasks.size)

        val paginatedTasks = if (filteredTasks.isEmpty()) {
            emptyList()
        } else {
            filteredTasks.subList(start, end)
        }

        mutableState.update {
            it.copy(
                filteredTasks = paginatedTasks,
                totalPages = totalPages,
                currentPage = currentPageValue,
                searchActive = searchActive.value,
                searchQuery = searchQuery.value,
                selectedPriority = selectedPriority.value,
                selectedCategory = selectedCategory.value
            )
        }
    }

    private fun applySearchFiltersToTasks(tasks: List<Task>): List<Task> {
        var filteredTasks = tasks

        // Apply text search
        if (searchQuery.value.isNotBlank()) {
            val searchTerms = searchQuery.value.lowercase().trim()
            filteredTasks = filteredTasks.filter { task ->
                task.title.lowercase().contains(searchTerms) ||
                        task.description.lowercase().contains(searchTerms) ||
                        task.category?.lowercase()?.contains(searchTerms) == true
            }
        }

        // Apply priority filter
        selectedPriority.value?.let { priority ->
            filteredTasks = filteredTasks.filter { it.priority == priority }
        }

        // Apply category filter
        selectedCategory.value?.let { category ->
            filteredTasks = filteredTasks.filter { it.category == category }
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

                // Check if wizard profile was recently updated using flag
                val currentProfile = mutableState.value.wizardProfile?.getOrNull()
                val wasRecentlyUpdated = if (profileRecentlyUpdated) {
                    val now = System.currentTimeMillis()
                    val timeSinceUpdate = now - lastProfileUpdateTime
                    val stillRecent = timeSinceUpdate < 3000  // 3 seconds

                    // Reset flag if more than 3 seconds have passed
                    if (!stillRecent) {
                        profileRecentlyUpdated = false
                    }

                    Log.d("TaskViewModel", "Using flag - timeSinceUpdate: ${timeSinceUpdate}ms, stillRecent: $stillRecent")
                    stillRecent
                } else {
                    false
                }

                Log.d("TaskViewModel", "loadData - wasRecentlyUpdated: $wasRecentlyUpdated, currentXP: ${currentProfile?.experience}")

                // Only reload wizard profile if it wasn't recently updated
                val wizardProfile = if (wasRecentlyUpdated && currentProfile != null) {
                    Log.d("TaskViewModel", "Using existing profile (recently updated) - XP: ${currentProfile.experience}")
                    currentProfile
                } else {
                    Log.d("TaskViewModel", "Loading fresh profile from database")
                    try {
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
                }

                val tasks = try {
                    taskRepository.getAllTasks(userId)
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Failed to get tasks: ${e.message}")
                    mutableState.update { it.copy(error = "Failed to load tasks: ${e.message}") }
                    emptyList()
                }

                allTasks = tasks

                val filteredTasks = applySearchFiltersToTasks(tasks)

                val totalPages = (filteredTasks.size + pageSize - 1) / pageSize
                val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

                val start = (currentPageValue - 1) * pageSize
                val end = minOf(start + pageSize, filteredTasks.size)

                val paginatedTasks = if (filteredTasks.isEmpty()) {
                    emptyList()
                } else {
                    filteredTasks.subList(start, end)
                }

                val profileToUse = if (wizardProfile != null) {
                    if (wizardProfile.outfit.isBlank() && currentProfile?.outfit?.isNotBlank() == true) {
                        wizardProfile.copy(outfit = currentProfile.outfit)
                    } else {
                        wizardProfile
                    }
                } else {
                    currentProfile
                }

                Log.d("TaskViewModel", "loadData final - XP: ${profileToUse?.experience}, totalTasks: ${profileToUse?.totalTasksCompleted}")

                mutableState.update {
                    it.copy(
                        wizardProfile = if (profileToUse != null) Result.success(profileToUse) else it.wizardProfile,
                        tasks = tasks,
                        filteredTasks = paginatedTasks,
                        totalPages = totalPages,
                        currentPage = currentPageValue,
                        isLoading = false,
                        searchActive = searchActive.value,
                        searchQuery = searchQuery.value,
                        selectedPriority = selectedPriority.value,
                        selectedCategory = selectedCategory.value
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

    fun getTutorialTasks(): List<Task> {
        val today = System.currentTimeMillis()

        return listOf(
            Task(
                id = -1,
                title = "Clean your bed",
                description = "Make your bed and tidy up your bedroom",
                priority = Priority.MEDIUM,
                userId = "",
                isCompleted = false,
                createdAt = today,
                category = "Chores",
                dueDate = today
            ),
            Task(
                id = -2,
                title = "Do the dishes",
                description = "Wash and put away all dirty dishes",
                priority = Priority.MEDIUM,
                userId = "",
                isCompleted = false,
                createdAt = today,
                category = "Chores",
                dueDate = today
            )
        )
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

                Log.d(
                    "TaskViewModel",
                    "BEFORE COMPLETION - totalTasksCompleted: ${wizardProfile.totalTasksCompleted}"
                )

                // Calculate task effects - ONLY GAINS for on-time completion
                val (hpGain, staminaGain, expGain) = calculateTaskEffects(
                    priority = task.priority,
                    isOnTime = task.dueDate?.let { System.currentTimeMillis() <= it } ?: true,
                    currentLevel = wizardProfile.level,
                    currentStamina = wizardProfile.stamina
                )

                // Check if this is late completion (negative rewards)
                val isLateCompletion = hpGain < 0

                var newHealth: Int
                var newStamina: Int
                var newExperience: Int
                var newLevel = wizardProfile.level
                var newMaxHealth = wizardProfile.maxHealth
                var newMaxStamina = wizardProfile.maxStamina

                val actualHpGain: Int
                val actualStaminaGain: Int

                if (isLateCompletion) {
                    // Late completion - apply penalties but still gain XP
                    newHealth = (wizardProfile.health + hpGain).coerceAtLeast(0)
                    newStamina = (wizardProfile.stamina + staminaGain).coerceAtLeast(0)
                    newExperience = wizardProfile.experience + expGain

                    actualHpGain = hpGain
                    actualStaminaGain = staminaGain
                } else {
                    // On-time completion - only gains
                    newHealth =
                        (wizardProfile.health + hpGain).coerceAtMost(wizardProfile.maxHealth)
                    newStamina =
                        (wizardProfile.stamina + staminaGain).coerceAtMost(wizardProfile.maxStamina)
                    newExperience = wizardProfile.experience + expGain

                    actualHpGain = hpGain
                    actualStaminaGain = staminaGain
                }

                // Check for level up
                val isLevelUp = newExperience >= EXP_PER_LEVEL
                if (isLevelUp && newLevel < MAX_LEVEL) {
                    newLevel++
                    newExperience -= EXP_PER_LEVEL

                    newMaxHealth = WizardProfile.calculateMaxHealth(newLevel)
                    newMaxStamina = WizardProfile.calculateMaxStamina(newLevel)

                    // On level up, fully restore HP and boost stamina
                    newHealth = newMaxHealth
                    newStamina = (wizardProfile.stamina + 10).coerceAtMost(newMaxStamina)

                    if (newLevel == 30) {
                        checkForLevel30Achievement(newLevel)
                    }
                }

                val newTotalTasksCompleted = wizardProfile.totalTasksCompleted + 1

                Log.d(
                    "TaskViewModel",
                    "AFTER COMPLETION - newTotalTasksCompleted: $newTotalTasksCompleted"
                )

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

                Log.d(
                    "TaskViewModel",
                    "UPDATED PROFILE - totalTasksCompleted: ${updatedProfile.totalTasksCompleted}, XP: ${updatedProfile.experience}"
                )

                // Update task completion status
                taskRepository.updateTaskCompletionStatus(taskId, true)

                // Update wizard profile in database (background)
                wizardRepository.updateWizardProfile(userId, updatedProfile)

                Log.d("TaskViewModel", "Profile updated in repository")

                // Show notification
                notificationService?.showTaskCompletionNotification(
                    task = task,
                    wizardProfile = updatedProfile,
                    hpGained = actualHpGain,
                    staminaGained = actualStaminaGain
                )

                // UPDATE UI IMMEDIATELY with new profile (don't wait for database)
                mutableState.update { currentState ->
                    val newState = currentState.copy(
                        wizardProfile = Result.success(updatedProfile),
                        isLoading = false
                    )
                    Log.d(
                        "TaskViewModel",
                        "State updated - profile totalTasks: ${newState.wizardProfile?.getOrNull()?.totalTasksCompleted}, XP: ${newState.wizardProfile?.getOrNull()?.experience}"
                    )
                    newState
                }

                // SET FLAG to prevent reload
                profileRecentlyUpdated = true
                lastProfileUpdateTime = System.currentTimeMillis()

                // Only reload task list (NOT wizard profile)
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

    fun completeTutorialTask(taskId: Int) {
        if (taskId < 0) {
            completedTutorialTasks.add(taskId)
        }
    }

    fun getIncompleteTutorialTasks(): List<Task> {
        return getTutorialTasks().filter { it.id !in completedTutorialTasks }
    }

    private suspend fun loadTasksOnly(userId: String) {
        try {
            val tasks = taskRepository.getAllTasks(userId)

            allTasks = tasks

            val filteredTasks = applySearchFiltersToTasks(tasks)

            val totalPages = (filteredTasks.size + pageSize - 1) / pageSize
            val currentPageValue = currentPage.value.coerceIn(1, maxOf(1, totalPages))

            val start = (currentPageValue - 1) * pageSize
            val end = minOf(start + pageSize, filteredTasks.size)

            val paginatedTasks = if (filteredTasks.isEmpty()) {
                emptyList()
            } else {
                filteredTasks.subList(start, end)
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
            // ON-TIME COMPLETION = GAINS ONLY
            isOnTime -> when (priority) {
                Priority.HIGH -> Triple(
                    (BASE_HP_GAIN * 1.5 * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * 2 * staminaEfficiency).toInt(),
                    (BASE_XP_GAIN * 1.5).toInt()  // 75 XP
                )

                Priority.MEDIUM -> Triple(
                    (BASE_HP_GAIN * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * 1.5 * staminaEfficiency).toInt(),
                    BASE_XP_GAIN  // 50 XP
                )

                Priority.LOW -> Triple(
                    (BASE_HP_GAIN * 0.5 * levelFactor).toInt(),
                    (BASE_STAMINA_GAIN * staminaEfficiency).toInt(),
                    (BASE_XP_GAIN * 0.7).toInt()  // 35 XP
                )
            }

            // LATE COMPLETION = PENALTIES BUT STILL GAIN SOME XP
            else -> when (priority) {
                Priority.HIGH -> Triple(-15, -5, (BASE_XP_GAIN * 0.3).toInt())  // 15 XP
                Priority.MEDIUM -> Triple(-10, -3, (BASE_XP_GAIN * 0.2).toInt())  // 10 XP
                Priority.LOW -> Triple(-5, -1, (BASE_XP_GAIN * 0.1).toInt())  // 5 XP
            }
        }
    }

    /**
     * Calculate damage taken when deleting a task
     */
    fun calculateDeletionDamage(priority: Priority): Int {
        return when (priority) {
            Priority.HIGH -> 20
            Priority.MEDIUM -> 15
            Priority.LOW -> 10
        }
    }

    /**
     * Delete task and apply HP damage to wizard
     */
    fun deleteTaskWithDamage(taskId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = wizardRepository.getCurrentUserId() ?: run {
                    mutableState.update { it.copy(error = "Not logged in") }
                    return@launch
                }

                val task = uiState.value.tasks.find { it.id == taskId }
                    ?: taskRepository.getTaskById(taskId)

                if (task != null) {
                    val damage = calculateDeletionDamage(task.priority)

                    val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull()

                    if (wizardProfile != null) {
                        val newHealth = (wizardProfile.health - damage).coerceAtLeast(0)

                        val updatedProfile = wizardProfile.copy(
                            health = newHealth
                        )

                        wizardRepository.updateWizardProfile(userId, updatedProfile)

                        mutableState.update { state ->
                            state.copy(
                                wizardProfile = Result.success(updatedProfile),
                                recentDamage = damage
                            )
                        }

                        Toast.makeText(
                            context,
                            "Task deleted! Took $damage HP damage!",
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModelScope.launch {
                            delay(2000)
                            mutableState.update { it.copy(recentDamage = null) }
                        }
                    }

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

    fun deleteTask(taskId: Int, onSuccess: () -> Unit) {
        deleteTaskWithDamage(taskId, onSuccess)
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

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    fun clearEditTaskError() {
        mutableEditTaskState.update { it.copy(error = null) }
    }

    fun resetEditTaskState() {
        mutableEditTaskState.value = EditTaskState()
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
            in 1..4 -> 20
            in 5..8 -> 25
            in 9..14 -> 30
            in 15..19 -> 40
            in 20..24 -> 50
            in 25..29 -> 60
            else -> 100
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
        val tasksCompleted =
            mutableState.value.wizardProfile?.getOrNull()?.consecutiveTasksCompleted ?: 0
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

    override fun onCleared() {
        super.onCleared()
        soundPool.release()
    }
}