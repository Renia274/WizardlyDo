package com.example.wizardlydo.room.tasks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wizardlydo.data.tasks.Priority
import com.example.wizardlydo.data.tasks.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val id: Int,

    @ColumnInfo(name = "user_id")
    val userId: String,

    val title: String,
    val description: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.MEDIUM,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_daily")
    val isDaily: Boolean = false,

    @ColumnInfo(name = "category")
    val category: String? = null
) {
    fun toDomain() = Task(
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