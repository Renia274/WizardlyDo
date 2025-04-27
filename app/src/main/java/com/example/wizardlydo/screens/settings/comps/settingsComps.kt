package com.example.wizardlydo.screens.settings.comps

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.comps.NotificationType
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: Any? = null,
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.padding(end = 16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (action != null) {
            action()
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun SettingsActionItem(
    icon: Any? = null,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun ReminderDaysPicker(
    days: Int,
    onDaysChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Remind me before task due date:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (day in 1..3) {
                Button(
                    onClick = { onDaysChange(day) },
                    modifier = Modifier.weight(1f),
                    enabled = day != days
                ) {
                    Text("$day ${if (day == 1) "day" else "days"}")
                }

                if (day < 3) {
                    Spacer(modifier = Modifier.padding(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You will be reminded $days ${if (days == 1) "day" else "days"} before the task is due.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onPasswordChange: (String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (currentPasswordVisible) R.drawable.ic_hide else R.drawable.ic_show
                                ),
                                contentDescription = if (currentPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (newPasswordVisible) R.drawable.ic_hide else R.drawable.ic_show
                                ),
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (confirmPasswordVisible) R.drawable.ic_hide else R.drawable.ic_show
                                ),
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword.length < 12) {
                        error = "Password must be at least 12 characters"
                    } else if (newPassword != confirmPassword) {
                        error = "Passwords don't match"
                    } else {
                        onPasswordChange(newPassword)
                    }
                }
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationSettingsSection(
    state: SettingsState,
    notificationPermissionGranted: Boolean,
    onInAppNotificationsChange: (Boolean) -> Unit,
    onDamageNotificationsChange: (Boolean) -> Unit,
    onEmailNotificationsChange: (Boolean) -> Unit,
    onPreviewDamageEmail: () -> Unit,
    onPreviewCriticalEmail: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onShowTestNotification: (String, NotificationType, Long) -> Unit
) {
    val context = LocalContext.current

    SettingsSection(title = "Notifications") {
        // In-App Notifications Toggle with permission handling
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "In-App Notifications",
            subtitle = if (notificationPermissionGranted) {
                "Show notifications within the app"
            } else {
                "Requires notification permission"
            },
            action = {
                Column {
                    Switch(
                        checked = state.inAppNotificationsEnabled && notificationPermissionGranted,
                        onCheckedChange = { enabled ->
                            if (enabled && !notificationPermissionGranted) {
                                // Show a dialog asking the user to go to settings
                                Toast.makeText(
                                    context,
                                    "Please allow notifications in app settings",
                                    Toast.LENGTH_LONG
                                ).show()

                                // This would trigger the permission dialog in a real app
                                onRequestNotificationPermission()

                                // For debug purposes - directly open app notification settings
                                try {
                                    val intent = Intent().apply {
                                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                        putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to app info settings if the above doesn't work
                                    val fallbackIntent = Intent().apply {
                                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.fromParts("package", context.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallbackIntent)
                                }
                            } else {
                                onInAppNotificationsChange(enabled)
                            }
                        },
                        enabled = true // Always enabled so users can click to trigger permission request
                    )
                    if (!notificationPermissionGranted) {
                        Text(
                            text = "Tap to enable permission",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        )

        // Add test notification button
        if (notificationPermissionGranted) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onShowTestNotification(
                        "Test notification working! 🔔",
                        NotificationType.INFO,
                        5000
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Test In-App Notification")
                }
            }

            Text(
                text = "Shows a test notification immediately",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Damage notifications toggle (only if in-app notifications are enabled and permission granted)
        if (state.inAppNotificationsEnabled && notificationPermissionGranted) {
            SettingsItem(
                title = "Character Damage Alerts",
                subtitle = "Warns you when missing tasks damages your wizard",
                action = {
                    Switch(
                        checked = state.damageNotificationsEnabled,
                        onCheckedChange = onDamageNotificationsChange
                    )
                }
            )
        }

        // Email notifications toggle
        SettingsItem(
            icon = painterResource(id = R.drawable.ic_email),
            title = "Email Notifications",
            subtitle = "Get important alerts via email",
            action = {
                Switch(
                    checked = state.emailNotificationsEnabled,
                    onCheckedChange = onEmailNotificationsChange
                )
            }
        )

        // Email preview buttons (only if email notifications are enabled)
        if (state.emailNotificationsEnabled && !state.email.isNullOrEmpty()) {
            EmailPreviewButtons(
                onPreviewDamageEmail = onPreviewDamageEmail,
                onPreviewCriticalEmail = onPreviewCriticalEmail
            )
        }
    }
}

@Composable
fun EmailPreviewButtons(
    onPreviewDamageEmail: () -> Unit,
    onPreviewCriticalEmail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Preview Email Notifications",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = onPreviewDamageEmail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Preview Damage Alert")
        }

        Button(
            onClick = onPreviewCriticalEmail,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Preview Critical Alert")
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}



