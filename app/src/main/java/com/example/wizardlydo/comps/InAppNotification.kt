package com.example.wizardlydo.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class NotificationType(val id: Int) {
    DAMAGE(1),
    WARNING(2),
    CRITICAL(3),
    INFO(4)
}

@Composable
fun InAppNotification(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: NotificationType = NotificationType.INFO
) {
    val backgroundColor = when (type) {
        NotificationType.INFO -> MaterialTheme.colorScheme.primaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.errorContainer
        NotificationType.CRITICAL -> MaterialTheme.colorScheme.error
        NotificationType.DAMAGE -> MaterialTheme.colorScheme.error
    }

    val textColor = when (type) {
        NotificationType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        NotificationType.CRITICAL -> MaterialTheme.colorScheme.onError
        NotificationType.DAMAGE -> MaterialTheme.colorScheme.error
    }

    LaunchedEffect(Unit) {
        delay(5000)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Add bell icon at the start of the notification
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification",
                tint = textColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textColor
                )
            }
        }
    }
}