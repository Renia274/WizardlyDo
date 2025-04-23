package com.example.wizardlydo.room.tasks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY due_date ASC")
    suspend fun getTasksByUser(userId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 0 ORDER BY due_date ASC")
    suspend fun getActiveTasksByUser(userId: Int): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_daily = 1")
    suspend fun getDailyTasks(userId: Int): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 1")
    suspend fun getCompletedTasks(userId: Int): List<TaskEntity>

    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE task_id = :taskId")
    suspend fun updateCompletionStatus(taskId: Int, isCompleted: Boolean)

    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_completed = 1")
    suspend fun getCompletedTaskCount(userId: Int): Int

    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND (:category IS NULL OR category = :category)
        ORDER BY 
            CASE WHEN due_date IS NULL THEN 1 ELSE 0 END,
            due_date ASC
    """)
    suspend fun getTasksByCategory(userId: Int, category: String?): List<TaskEntity>
}