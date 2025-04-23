package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.Priority
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.TaskFilter
import com.example.wizardlydo.data.models.TaskUiState
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.lang.Exception

@KoinViewModel
class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val auth: FirebaseAuth = Firebase.auth,
    private val wizardRepository: WizardRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(TaskUiState())
    val uiState = mutableState.asStateFlow()

    val currentUserId = MutableStateFlow<String?>(auth.currentUser?.uid)
    val currentUserIdState = currentUserId.asStateFlow()

    companion object {
        private const val EXP_PER_LEVEL = 1000
    }

    init {
        loadData()
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                taskRepository.insertTask(task.copy(userId = userId))
            }
        }
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