package com.example.wizardlydo.utilities



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class TaskBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val taskRepository: TaskRepository by inject()
    private val wizardRepository: WizardRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        if (taskId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    "COMPLETE_TASK" -> {
                        completeTask(taskId)

                        // Cancel the notification
                        val notificationService = TaskNotificationService(context)
                        notificationService.cancelTaskNotification(taskId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun completeTask(taskId: Int) {
        try {
            val task = taskRepository.getTaskById(taskId) ?: return
            val userId = task.userId
            val wizardProfile = wizardRepository.getWizardProfile(userId).getOrNull() ?: return

            val now = System.currentTimeMillis()
            val isOnTime = task.dueDate?.let { dueDate -> now <= dueDate } ?: true

            // Calculate task effects similar to TaskViewModel logic
            val (hpChange, staminaChange, expGain) = when {
                isOnTime -> when (task.priority) {
                    Priority.HIGH -> Triple(15, 20, 50)
                    Priority.MEDIUM -> Triple(10, 15, 30)
                    Priority.LOW -> Triple(5, 10, 20)
                }
                else -> when (task.priority) {
                    Priority.HIGH -> Triple(-20, -10, 5)
                    Priority.MEDIUM -> Triple(-15, -5, 3)
                    Priority.LOW -> Triple(-10, 0, 1)
                }
            }

            val updatedProfile = wizardProfile.copy(
                health = (wizardProfile.health + hpChange).coerceIn(0, wizardProfile.maxHealth),
                stamina = (wizardProfile.stamina + staminaChange).coerceIn(0, 100),
                experience = wizardProfile.experience + expGain
            ).let { profile ->
                // Check level up similar to TaskViewModel
                var newLevel = profile.level
                var remainingExp = profile.experience
                val expPerLevel = 1000

                while (remainingExp >= expPerLevel) {
                    remainingExp -= expPerLevel
                    newLevel++
                }

                profile.copy(level = newLevel, experience = remainingExp)
            }

            wizardRepository.updateWizardProfile(userId, updatedProfile)
            taskRepository.updateTaskCompletionStatus(taskId, true)
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
}