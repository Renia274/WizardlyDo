package com.example.wizardlydo.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.wizardlydo.MainActivity
import com.example.wizardlydo.R
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task
import com.example.wizardlydo.data.wizard.WizardProfile
import java.util.concurrent.TimeUnit

class TaskNotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val TASK_CHANNEL_ID = "task_reminder"
        private const val TASK_GROUP_KEY = "com.example.wizardlydo.TASK_NOTIFICATIONS"
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

        //  high priority tasks
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


    fun showTaskCompletionNotification(
        task: Task,
        wizardProfile: WizardProfile,
        hpGained: Int?=null,
        staminaGained: Int?=null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id + 2000,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val expPerLevel = 1000
        val currentLevel = wizardProfile.level
        val currentExp = wizardProfile.experience
        val expToNextLevel = expPerLevel - currentExp
        val expPerTask = expPerLevel / getTasksRequiredForLevel(currentLevel)
        val tasksToNextLevel = (expToNextLevel / expPerTask.toFloat()).toInt().coerceAtLeast(1)


        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle("Task Completed!")
            .bigText(
                """
            ${task.title} has been completed!
            
          
            Current HP: ${wizardProfile.health}/${wizardProfile.maxHealth}
            Current Stamina: ${wizardProfile.stamina}/100
            Level: $currentLevel (${tasksToNextLevel} set of tasks to next level)
            
            Keep up the good work!
        """.trimIndent()
            )

        // Set notification color based on priority
        val color = when (task.priority) {
            Priority.HIGH -> R.color.high_priority
            Priority.MEDIUM -> R.color.medium_priority
            Priority.LOW -> R.color.low_priority
        }

        val builder = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_complete)
            .setContentTitle("Task Completed!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, color))
            .setStyle(style)

        notificationManager.notify(task.id + 200000, builder.build())
    }

    private fun getTasksRequiredForLevel(level: Int): Int {
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




    fun showTaskCreatedNotification(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id + 1000,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("""
            ${task.description}
            
            Priority: ${task.priority.name}
            ${if (task.isDaily) "Repeats: Daily" else ""}
            ${task.category?.let { "Category: $it" } ?: ""}
        """.trimIndent())
            .setBigContentTitle("New Task Created")

        // Add due date information if available
        task.dueDate?.let {
            val daysRemaining = task.getDaysRemaining() ?: 0
            if (daysRemaining > 0) {
                bigTextStyle.setSummaryText("Due in $daysRemaining day${if (daysRemaining != 1) "s" else ""}")
            } else {
                bigTextStyle.setSummaryText("Due today!")
            }
        }

        val builder = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_task)
            .setContentTitle("New Task: ${task.title}")
            .setContentText("Task has been successfully created")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(bigTextStyle)

        // color based on priority
        when (task.priority) {
            Priority.HIGH -> {
                builder.setColorized(true)
                    .setColor(ContextCompat.getColor(context, R.color.high_priority))
            }

            Priority.MEDIUM -> {
                builder.setColorized(true)
                    .setColor(ContextCompat.getColor(context, R.color.medium_priority))
            }

            Priority.LOW -> {
                builder.setColorized(true)
                    .setColor(ContextCompat.getColor(context, R.color.low_priority))
            }
        }

        notificationManager.notify(task.id + 100000, builder.build())
    }

    fun scheduleTaskNotification(task: Task) {
        task.dueDate?.let { dueDate ->
            val currentTime = System.currentTimeMillis()

            // Calculate the notification times
            // 1. If due date is more than 3 days away, schedule for 3 days before
            // 2. If due date is less than 3 days away but more than 1 day, schedule for 1 day before
            // 3. If due date is less than 1 day away, schedule for 1 hour before
            // 4. Also schedule for the exact due time

            val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val oneHourInMillis = 60 * 60 * 1000L

            val timeUntilDue = dueDate - currentTime

            if (timeUntilDue > 0) {
                val scheduleTimes = mutableListOf<Long>()

                // Add specific reminders based on how far away the due date is
                if (timeUntilDue > threeDaysInMillis) {
                    scheduleTimes.add(dueDate - threeDaysInMillis) // 3 days before
                }

                if (timeUntilDue > oneDayInMillis) {
                    scheduleTimes.add(dueDate - oneDayInMillis) // 1 day before
                }

                if (timeUntilDue > oneHourInMillis) {
                    scheduleTimes.add(dueDate - oneHourInMillis) // 1 hour before
                }

                // Add exact due time reminder
                scheduleTimes.add(dueDate)

                // Schedule each notification
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
            TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0) + 1 // +1 to count current partial day
        }
    }

    fun cancelTaskNotification(taskId: Int) {
        notificationManager.cancel(taskId)
    }
}