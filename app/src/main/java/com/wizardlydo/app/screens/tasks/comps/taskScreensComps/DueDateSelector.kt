package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable
fun DueDateSelector(
    dueDate: Long?,
    dateFormatter: DateFormat,
    onDatePickerTrigger: () -> Unit
) {
    OutlinedButton(
        onClick = onDatePickerTrigger,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(dueDate?.let { "Due: ${dateFormatter.format(Date(it))}" } ?: "Select Due Date")
    }
}
