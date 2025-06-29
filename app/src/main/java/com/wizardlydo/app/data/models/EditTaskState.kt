package com.wizardlydo.app.data.models

import com.wizardlydo.app.data.tasks.Priority
import com.wizardlydo.app.data.tasks.Task

data class EditTaskState(
    val task: Task? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)


enum class EditTaskField {
    /**
     * The title of the task.
     */
    TITLE,

    /**
     * The description of the task.
     */
    DESCRIPTION,

    /**
     * The due date of the task.
     */
    DUE_DATE,

    /**
     * The priority level of the task.
     */
    PRIORITY,

    /**
     * The category of the task.
     */
    CATEGORY,


}
