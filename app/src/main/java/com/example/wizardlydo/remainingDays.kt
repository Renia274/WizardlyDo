package com.example.wizardlydo

import com.example.wizardlydo.data.Task
import java.util.concurrent.TimeUnit

fun Task.getDaysRemaining(): Int? {
    return dueDate?.let { dueDateMillis ->
        val currentTime = System.currentTimeMillis()
        val diff = dueDateMillis - currentTime
        TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0) + 1 // +1 to count current partial day
    }
}