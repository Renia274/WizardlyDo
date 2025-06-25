package com.wizardlydo.app.repository.tasks

import com.wizardlydo.app.data.tasks.Task
import com.wizardlydo.app.room.tasks.TaskDao
import com.wizardlydo.app.room.tasks.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Get all tasks for a user, ordered by due date
     */
    suspend fun getAllTasks(userId: String): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getTasksByUser(userId).map { it.toDomain() }
    }

    /**
     * Get active (incomplete) tasks for a user
     */
    suspend fun getActiveTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getActiveTasksByUser(userId).map { it.toDomain() }
    }

    /**
     * Get completed tasks for a user
     */
    suspend fun getCompletedTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getCompletedTasks(userId).map { it.toDomain() }
    }

    /**
     * Get a specific task by ID
     */
    suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)?.toDomain()
    }

    /**
     * Get count of completed tasks for a user
     */
    suspend fun getCompletedTaskCount(userId: Int): Int = withContext(Dispatchers.IO) {
        taskDao.getCompletedTaskCount(userId)
    }

    /**
     * Get tasks by category for a user
     */
    suspend fun getTasksByCategory(userId: Int, category: String?): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getTasksByCategory(userId, category).map { it.toDomain() }
    }

    /**
     * Insert a new task
     */
    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insert(task.toEntity())
    }

    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.update(task.toEntity())
    }

    /**
     * Delete a task
     */
    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.delete(task.toEntity())
    }

    /**
     * Get tasks that are due but not completed yet
     */
    suspend fun getDueTasks(userId: String): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getDueTasks(userId).map { it.toDomain() }
    }

    /**
     * Get tasks that are coming up in the near future
     * @param userId The user ID
     * @param currentTime Current time in milliseconds
     * @param targetDate Future time in milliseconds to check for tasks due by then
     */
    suspend fun getUpcomingTasks(userId: String, currentTime: Long, targetDate: Long): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getUpcomingTasks(userId, currentTime, targetDate).map { it.toDomain() }
    }

    /**
     * Update task completion status
     */
    suspend fun updateTaskCompletionStatus(taskId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateCompletionStatus(taskId, isCompleted)
    }

    /**
     * Convert Task domain model to TaskEntity
     */
    private fun Task.toEntity() = TaskEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        dueDate = dueDate,
        priority = priority,
        createdAt = createdAt,
        category = category
    )

    /**
     * Convert TaskEntity to Task domain model
     */
    private fun TaskEntity.toDomain() = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        dueDate = dueDate,
        priority = priority,
        createdAt = createdAt,
        category = category
    )
}