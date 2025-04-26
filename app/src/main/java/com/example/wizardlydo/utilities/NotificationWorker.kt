package com.example.wizardlydo.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wizardlydo.R
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import kotlin.random.Random


class NotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: WizardRepository,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = repository.getCurrentUserId() ?: return Result.failure()

        return try {
            val profile = repository.getWizardProfile(userId).getOrNull() ?: return Result.failure()

            if (!profile.pushNotificationsEnabled) return Result.success()

            val tasks = taskRepository.getDueTasks(userId)
            val damage = calculateDamage(tasks)
            val newHealth = (profile.health - damage).coerceAtLeast(0)

            repository.updateWizardProfile(
                userId,
                profile.copy(health = newHealth)
            ).getOrThrow()

            if (newHealth <= 0) {
                sendNotification("Your wizard has perished! 💀", "Revive now to continue!")
            } else if (damage > 0) {
                sendNotification("Damage Taken! ⚠️", "Your wizard lost $damage HP!")
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun calculateDamage(tasks: List<Task>): Int {
        val now = System.currentTimeMillis()
        return tasks.sumOf { task ->
            task.dueDate?.let { dueDate ->
                // Calculate days overdue (ensure positive value)
                val overdueMillis = (now - dueDate).coerceAtLeast(0)
                (overdueMillis / (1000 * 60 * 60 * 24)).toInt()
            } ?: 0
        } * 10
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager ?: return

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "damage_channel",
                "Damage Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Wizard health notifications"
                notificationManager.createNotificationChannel(this)
            }
        }

        // Ensure notification icon exists in resources
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "damage_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_wizard)
            .setAutoCancel(true)

        try {
            notificationManager.notify(Random.nextInt(), notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}