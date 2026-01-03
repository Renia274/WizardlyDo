package com.wizardlydo.app.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.wizardlydo.app.MainActivity
import com.wizardlydo.app.R
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.data.wizard.WizardProfile
import java.util.concurrent.TimeUnit

class TaskNotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val TASK_CHANNEL_ID = "task_reminder"
        private const val TASK_GROUP_KEY = "com.wizardlydo.app.app.TASK_NOTIFICATIONS"
    }

    fun showTaskNotification(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, TaskBroadcastReceiver::class.java).apply {
            action = "COMPLETE_TASK"
            putExtra("TASK_ID", task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            completeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_task)
            .setContentTitle(task.title)
            .setContentText(task.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(TASK_GROUP_KEY)
            .addAction(R.drawable.ic_check, "Complete", completePendingIntent)

        // High priority tasks
        if (task.priority == Priority.HIGH) {
            builder.setColorized(true)
                .setColor(ContextCompat.getColor(context, R.color.high_priority))
        }

        val style = NotificationCompat.BigTextStyle()
            .bigText("${task.description}\n\nPriority: ${task.priority.name}")
            .setBigContentTitle(task.title)

        task.dueDate?.let {
            val daysRemaining = task.getDaysRemaining() ?: 0
            if (daysRemaining >= 0) {
                style.setSummaryText("Due in $daysRemaining day${if (daysRemaining != 1) "s" else ""}")
            } else {
                style.setSummaryText("OVERDUE by ${-daysRemaining} day${if (-daysRemaining != 1) "s" else ""}")
            }
        }

        builder.setStyle(style)

        notificationManager.notify(task.id, builder.build())
    }

    /**
     * Show level-up notification with summary of what user accomplished
     */
    fun showLevelUpNotification(
        newLevel: Int,
        wizardProfile: WizardProfile,
        tasksCompletedThisLevel: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            newLevel + 3000,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate previous level range
        val previousLevel = newLevel - 1
        val levelRange = when (previousLevel) {
            in 1..4 -> "1-4"
            in 5..8 -> "5-8"
            in 9..14 -> "9-14"
            in 15..19 -> "15-19"
            in 20..24 -> "20-24"
            in 25..29 -> "25-29"
            else -> "30"
        }

        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle("🎉 Level $newLevel Unlocked!")
            .bigText(
                """
            Congratulations! You've reached Level $newLevel!
            
            Level Set $levelRange Summary:
            ✓ Completed $tasksCompletedThisLevel tasks
            ✓ HP: ${wizardProfile.health}/${wizardProfile.maxHealth}
            ✓ Stamina: ${wizardProfile.stamina}/${wizardProfile.maxStamina}
            
            Keep up the amazing work!
        """.trimIndent()
            )

        val builder = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_complete)
            .setContentTitle("Level $newLevel Achieved!")
            .setContentText("Tap to see your progress")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.high_priority))
            .setStyle(style)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        notificationManager.notify(newLevel + 400000, builder.build())
    }

    fun scheduleTaskNotification(task: Task) {
        task.dueDate?.let { dueDate ->
            val currentTime = System.currentTimeMillis()

            val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val oneHourInMillis = 60 * 60 * 1000L

            val timeUntilDue = dueDate - currentTime

            if (timeUntilDue > 0) {
                val scheduleTimes = mutableListOf<Long>()

                if (timeUntilDue > threeDaysInMillis) {
                    scheduleTimes.add(dueDate - threeDaysInMillis)
                }

                if (timeUntilDue > oneDayInMillis) {
                    scheduleTimes.add(dueDate - oneDayInMillis)
                }

                if (timeUntilDue > oneHourInMillis) {
                    scheduleTimes.add(dueDate - oneHourInMillis)
                }

                scheduleTimes.add(dueDate)

                scheduleTimes.forEachIndexed { index, scheduleTime ->
                    val delayMillis = scheduleTime - currentTime
                    if (delayMillis > 0) {
                        val notificationWorkRequest =
                            OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                                .setInputData(
                                    workDataOf(
                                        "TASK_ID" to task.id,
                                        "TASK_TITLE" to task.title,
                                        "TASK_DESCRIPTION" to task.description,
                                        "TASK_PRIORITY" to task.priority.name,
                                        "TASK_DUE_DATE" to task.dueDate
                                    )
                                )
                                .build()

                        WorkManager.getInstance(context)
                            .enqueue(notificationWorkRequest)
                    }
                }
            }
        }
    }

    private fun Task.getDaysRemaining(): Int? {
        return dueDate?.let { dueDateMillis ->
            val currentTime = System.currentTimeMillis()
            val diff = dueDateMillis - currentTime
            TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0) + 1
        }
    }

    fun cancelTaskNotification(taskId: Int) {
        notificationManager.cancel(taskId)
    }
}