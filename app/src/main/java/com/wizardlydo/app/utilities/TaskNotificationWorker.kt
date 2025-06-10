package com.wizardlydo.app.utilities

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task

class TaskNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskId = inputData.getInt("TASK_ID", -1)
        val taskTitle = inputData.getString("TASK_TITLE") ?: return Result.failure()
        val taskDescription = inputData.getString("TASK_DESCRIPTION") ?: ""
        val priorityName = inputData.getString("TASK_PRIORITY") ?: "MEDIUM"
        val taskDueDate = inputData.getLong("TASK_DUE_DATE", 0L)

        if (taskId == -1 || taskDueDate == 0L) {
            return Result.failure()
        }

        val priority = try {
            Priority.valueOf(priorityName)
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM
        }

        val task = Task(
            id = taskId,
            userId = "", // Not needed for notification
            title = taskTitle,
            description = taskDescription,
            isCompleted = false,
            dueDate = taskDueDate,
            priority = priority,
            createdAt = System.currentTimeMillis(),
            category = null
        )

        val notificationService = TaskNotificationService(context)
        notificationService.showTaskNotification(task)

        return Result.success()
    }
}
