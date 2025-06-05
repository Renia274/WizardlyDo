package com.example.wizardlydo.data.tasks

import com.example.wizardlydo.room.tasks.TaskEntity

data class Task(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String? = null
) {
    fun toEntity() = TaskEntity(
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

enum class Priority {
    LOW, MEDIUM, HIGH
}