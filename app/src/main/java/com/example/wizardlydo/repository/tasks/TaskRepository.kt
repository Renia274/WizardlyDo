package com.example.wizardlydo.repository.tasks

import com.example.wizardlydo.data.Task
import com.example.wizardlydo.room.tasks.TaskDao
import com.example.wizardlydo.room.tasks.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun getAllTasks(userId: String): List<Task> =
        taskDao.getTasksByUser(userId).map { it.toDomain() }

    suspend fun getActiveTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getActiveTasksByUser(userId).map { it.toDomain() }
    }

    suspend fun getDailyTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getDailyTasks(userId).map { it.toDomain() }
    }

    suspend fun getCompletedTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getCompletedTasks(userId).map { it.toDomain() }
    }

    suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)?.toDomain()
    }

    suspend fun getCompletedTaskCount(userId: Int): Int = withContext(Dispatchers.IO) {
        taskDao.getCompletedTaskCount(userId)
    }

    suspend fun getTasksByCategory(userId: Int, category: String?): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getTasksByCategory(userId, category).map { it.toDomain() }
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insert(task.toEntity())
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.update(task.toEntity())
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.delete(task.toEntity())
    }

    /**
     * Gets tasks that are due but not completed yet
     */
    suspend fun getDueTasks(userId: String): List<Task> {
        return taskDao.getDueTasks(userId).map { it.toDomain() }
    }

    /**
     * Gets tasks that are coming up in the near future
     * @param currentTime Current time in milliseconds
     * @param targetDate Future time in milliseconds to check for tasks due by then
     */
    suspend fun getUpcomingTasks(userId: String, currentTime: Long, targetDate: Long): List<Task> {
        return taskDao.getUpcomingTasks(userId, currentTime, targetDate).map { it.toDomain() }
    }

    suspend fun updateTaskCompletionStatus(taskId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateCompletionStatus(taskId, isCompleted)
    }



    private fun Task.toEntity() = TaskEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        dueDate = dueDate,
        priority = priority,
        createdAt = createdAt,
        isDaily = isDaily,
        category = category
    )

    private fun TaskEntity.toDomain() = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        dueDate = dueDate,
        priority = priority,
        createdAt = createdAt,
        isDaily = isDaily,
        category = category
    )
}