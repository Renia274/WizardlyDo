package com.example.wizardlydo.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Colors based on notification type
    val containerColor = when (type) {
        NotificationType.INFO -> MaterialTheme.colorScheme.primaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.errorContainer
        NotificationType.CRITICAL -> MaterialTheme.colorScheme.error
        NotificationType.DAMAGE -> MaterialTheme.colorScheme.error
    }

    val contentColor = when (type) {
        NotificationType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        NotificationType.CRITICAL -> MaterialTheme.colorScheme.onError
        NotificationType.DAMAGE -> MaterialTheme.colorScheme.onError
    }

    // Show the snackbar when composable is first launched
    LaunchedEffect(message) {  // Changed to watch message changes
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                actionLabel = "Dismiss",
                withDismissAction = true
            )
            onDismiss() // Callback when snackbar is dismissed
        }
    }

    // Snackbar host that will display the notification
    Box(modifier = modifier.fillMaxWidth()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        ) { snackbarData ->  // Changed parameter name to snackbarData
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = containerColor,
                contentColor = contentColor,
                action = {
                    TextButton(
                        onClick = { snackbarData.dismiss() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor
                        )
                    ) {
                        Text("DISMISS")
                    }
                },
                content = {  // Explicit content block
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = contentColor
                        )
                        Text(
                            text = snackbarData.message,  // Access message from snackbarData
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                    }
                }
            )
        }
    }
}