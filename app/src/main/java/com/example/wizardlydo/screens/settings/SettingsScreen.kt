package com.example.wizardlydo.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.wizardlydo.R
import com.example.wizardlydo.comps.InAppNotification
import com.example.wizardlydo.comps.NotificationType
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.screens.settings.comps.NotificationSettingsSection
import com.example.wizardlydo.screens.settings.comps.PasswordChangeDialog
import com.example.wizardlydo.screens.settings.comps.ReminderDaysPicker
import com.example.wizardlydo.screens.settings.comps.SettingsActionItem
import com.example.wizardlydo.screens.settings.comps.SettingsItem
import com.example.wizardlydo.screens.settings.comps.SettingsSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val notificationPermissionGranted by viewModel.notificationPermissionGranted.collectAsState()
    val notification by viewModel.activeNotification.collectAsState()

    Box(Modifier.fillMaxSize()) {
        SettingsContent(
            state = state,
            notificationPermissionGranted = notificationPermissionGranted,
            onBack = onBack,
            onLogoutConfirmed = {
                viewModel.logout()
                onLogout()
            },
            onChangePassword = { currentPassword, newPassword ->
                viewModel.changePassword(
                    newPassword = newPassword,
                    currentPassword = currentPassword,
                    onSuccess = {},
                    onError = {}
                )
            },
            onReminderEnabledChange = viewModel::updateReminderEnabled,
            onReminderDaysChange = viewModel::updateReminderDays,

            onEmailNotificationsChange = viewModel::updateEmailNotifications,
            onPreviewDamageEmail = viewModel::sendDamagePreviewEmail,
            onPreviewCriticalEmail = viewModel::sendCriticalPreviewEmail


        )

        // Display notification on top of the settings screen
        notification?.let { notif ->
            InAppNotification(
                message = notif.message,
                type = notif.type,
                onDismiss = { viewModel.clearNotification() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .zIndex(10f)
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    notificationPermissionGranted: Boolean,
    onBack: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDaysChange: (Int) -> Unit,
    onEmailNotificationsChange: (Boolean) -> Unit,
    onPreviewDamageEmail: () -> Unit,
    onPreviewCriticalEmail: () -> Unit,

) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showPasswordDialog) {
        PasswordChangeDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordChange = { newPassword ->
                onChangePassword("", newPassword)
                showPasswordDialog = false
                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    onLogoutConfirmed()
                    showLogoutDialog = false
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Email",
                    subtitle = state.email ?: "Not logged in"
                )
                SettingsActionItem(
                    icon = painterResource(id = R.drawable.ic_lock),
                    title = "Change Password",
                    onClick = { showPasswordDialog = true }
                )
                SettingsActionItem(
                    icon = painterResource(id = R.drawable.ic_logout),
                    title = "Logout",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }

            SettingsSection(title = "Reminders") {
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_alarm),
                    title = "Enable Task Reminders",
                    action = {
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = onReminderEnabledChange
                        )
                    }
                )

                if (state.reminderEnabled) {
                    ReminderDaysPicker(
                        days = state.reminderDays,
                        onDaysChange = onReminderDaysChange
                    )




                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            NotificationSettingsSection(
                state = state,
                onEmailNotificationsChange = onEmailNotificationsChange,
                onPreviewDamageEmail = onPreviewDamageEmail,
                onPreviewCriticalEmail = onPreviewCriticalEmail,

            )

            SettingsSection(title = "About WizardlyDo") {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "WizardlyDo: A Gamified To-Do List App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "WizardlyDo turns your boring task list into an exciting adventure! " +
                                    "Complete tasks to gain experience and level up your wizard character.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "⚠️ Task Warning System ⚠️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Your wizard takes damage when tasks are overdue. If health reaches zero, " +
                                    "you'll need to revive your character! Stay on top of your tasks to keep " +
                                    "your wizard healthy and strong.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenNotificationPreview() {
    WizardlyDoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Settings Preview", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Preview a notification
            InAppNotification(
                message = "This is how notifications will appear",
                type = NotificationType.INFO,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WizardlyDoTheme {
        SettingsContent(
            state = SettingsState(
                email = "wizard@example.com",
                reminderEnabled = true,
                reminderDays = 3,
                inAppNotificationsEnabled = true,
                damageNotificationsEnabled = false,
                emailNotificationsEnabled = true
            ),
            notificationPermissionGranted = true,
            onBack = {},
            onLogoutConfirmed = {},
            onChangePassword = { _, _ -> },
            onReminderEnabledChange = {},
            onReminderDaysChange = {},
            onEmailNotificationsChange = {},
            onPreviewDamageEmail = {},
            onPreviewCriticalEmail = {},

        )
    }
}