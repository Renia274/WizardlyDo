package com.example.wizardlydo.repository.tasks

import com.example.wizardlydo.data.Task
import com.example.wizardlydo.room.tasks.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getAllTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getTasksByUser(userId).map { it.toDomain() }
    }

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

    suspend fun updateTaskCompletionStatus(taskId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateCompletionStatus(taskId, isCompleted)
    }


}