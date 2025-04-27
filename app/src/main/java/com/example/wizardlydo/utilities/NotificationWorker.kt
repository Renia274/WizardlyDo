package com.example.wizardlydo.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wizardlydo.MainActivity
import com.example.wizardlydo.R
import com.example.wizardlydo.data.Task
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random


class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: WizardRepository by inject()
    private val taskRepository: TaskRepository by inject()
    private val emailSender = EmailSender(applicationContext)

    override suspend fun doWork(): Result {
        val userId = repository.getCurrentUserId() ?: return Result.failure()

        return try {
            val profile = repository.getWizardProfile(userId).getOrNull() ?: return Result.failure()

            // Process tasks and calculate damage
            val tasks = taskRepository.getDueTasks(userId)
            val damage = calculateDamage(tasks)
            val newHealth = (profile.health - damage).coerceAtLeast(0)

            // Update wizard health
            repository.updateWizardProfile(
                userId,
                profile.copy(health = newHealth)
            ).getOrThrow()

            // Send push notifications
            if (profile.inAppNotificationsEnabled) {
                sendPostNotifications(profile, damage, newHealth, tasks)
            }

            // Send email notifications if enabled
            if (profile.emailNotificationsEnabled) {
                sendEmailNotifications(profile, damage, newHealth, tasks)
            }

            // Send task reminders if enabled
            if (profile.reminderEnabled) {
                sendTaskReminders(userId, profile)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendPostNotifications(
        profile: WizardProfile,
        damage: Int,
        newHealth: Int,
        tasks: List<Task>
    ) {
        // Check permission first
        if (!NotificationPermissionHandler.checkPermission(applicationContext)) {
            return
        }

        if (!profile.inAppNotificationsEnabled || !profile.damageNotificationsEnabled) return

        if (newHealth <= 0) {
            sendNotification(
                "Your wizard has perished! 💀",
                "Revive now to continue your journey!"
            )
        } else if (damage > 0) {
            sendNotification(
                "Damage Taken! ⚠️",
                "Your wizard lost $damage HP! Current health: $newHealth/${profile.maxHealth}"
            )
        }
    }

    private fun sendEmailNotifications(
        profile: WizardProfile,
        damage: Int,
        newHealth: Int,
        tasks: List<Task>
    ) {
        val userEmail = profile.email

        // Only send if there's damage and damage notifications are enabled
        if (damage <= 0 || !profile.damageNotificationsEnabled) return

        // Convert task objects to task names for the template
        val taskNames = tasks.map { it.title }

        // Choose the appropriate template based on health status
        val emailContent = when {
            newHealth <= 0 -> WizardEmailTemplates.getPerishedWizardTemplate(taskNames)
            newHealth <= 20 -> WizardEmailTemplates.getCriticalHealthTemplate(damage, newHealth, profile.maxHealth, taskNames)
            else -> WizardEmailTemplates.getDamageEmailTemplate(damage, newHealth, profile.maxHealth, taskNames)
        }

        // Choose the appropriate subject based on health status
        val emailSubject = when {
            newHealth <= 0 -> "💀 YOUR WIZARD HAS PERISHED! 💀"
            newHealth <= 20 -> "⚠️ URGENT: Your Wizard Is In Critical Danger!"
            else -> "🧙‍♂️ Your Wizard Has Taken Damage!"
        }

        // Send the email
        emailSender.sendEmail(userEmail, emailSubject, emailContent)
    }

    private suspend fun sendTaskReminders(userId: String, profile: WizardProfile) {
        try {
            // Get upcoming tasks using the repository method
            val today = System.currentTimeMillis()
            val reminderDaysMillis = profile.reminderDays * 24 * 60 * 60 * 1000L
            val targetDate = today + reminderDaysMillis

            // Use the proper repository method for upcoming tasks
            val upcomingTasks = taskRepository.getUpcomingTasks(userId, targetDate)

            if (upcomingTasks.isEmpty()) {
                return
            }

            // Send push notifications for each task
            upcomingTasks.forEach { task ->
                sendTaskReminderNotification(task)
            }

            // Send email if enabled
            if (profile.emailNotificationsEnabled && upcomingTasks.isNotEmpty()) {
                profile.email.let { userEmail ->
                    val taskNames = upcomingTasks.map { it.title }
                    val emailContent = WizardEmailTemplates.getTaskReminderTemplate(
                        taskNames = taskNames,
                        daysUntilDue = profile.reminderDays
                    )

                    emailSender.sendEmail(
                        userEmail,
                        "🧙‍♂️ Task Reminder from WizardlyDo",
                        emailContent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "damage_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_wizard)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(Random.nextInt(), notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendTaskReminderNotification(task: Task) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager ?: return

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "reminder_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Task reminder notifications"
                notificationManager.createNotificationChannel(this)
            }
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "reminder_channel")
            .setContentTitle("Task Reminder: ${task.title}")
            .setContentText("Due soon. Complete to prevent wizard damage!")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(task.id.hashCode(), notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
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
        } * 10  // 10 damage points per day overdue
    }
}