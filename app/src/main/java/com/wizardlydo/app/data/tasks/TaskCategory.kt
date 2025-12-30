package com.wizardlydo.app.data.tasks

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.wizardlydo.app.R

data class TaskCategory(
    val name: String,
    @DrawableRes val iconRes: Int,
    val color: Color
)

fun getTaskCategories(): List<TaskCategory> {
    return listOf(
        TaskCategory("School", R.drawable.ic_school, Color(0xFF2196F3)), // Blue
        TaskCategory("Chores", R.drawable.ic_home, Color(0xFF4CAF50)), // Green
        TaskCategory("Work", R.drawable.ic_work, Color(0xFFFF9800)), // Orange
        TaskCategory("Personal", R.drawable.ic_person, Color(0xFF9C27B0)), // Purple
        TaskCategory("Health", R.drawable.ic_health, Color(0xFFE91E63)), // Pink
        TaskCategory("Fitness", R.drawable.ic_fitness, Color(0xFFF44336)), // Red
        TaskCategory("Shopping", R.drawable.ic_shopping, Color(0xFF00BCD4)), // Cyan
        TaskCategory("Finance", R.drawable.ic_finance, Color(0xFF009688)), // Teal
        TaskCategory("Other", R.drawable.ic_star, Color(0xFF9E9E9E)) // Gray
    )
}

// Function to get category by name
fun getCategoryByName(name: String): TaskCategory? {
    return getTaskCategories().find { it.name == name }
}