package com.example.wizardlydo.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.screens.settings.comps.DeleteAccountDialog
import com.example.wizardlydo.screens.settings.comps.PasswordChangeDialog
import com.example.wizardlydo.screens.settings.comps.SettingsActionItem
import com.example.wizardlydo.screens.settings.comps.SettingsItem
import com.example.wizardlydo.screens.settings.comps.SettingsSection
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val aboutTitle by viewModel.aboutTitle.collectAsState()
    val aboutDescription by viewModel.aboutDescription.collectAsState()
    val warningTitle by viewModel.warningTitle.collectAsState()
    val warningDescription by viewModel.warningDescription.collectAsState()
    val context = LocalContext.current


    Box(Modifier.fillMaxSize()) {
        SettingsContent(
            state = state,
            aboutTitle = aboutTitle,
            aboutDescription = aboutDescription,
            warningTitle = warningTitle,
            warningDescription = warningDescription,
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
            onDeleteAccount = { password ->
                viewModel.deleteAccount(
                    currentPassword = password,
                    onSuccess = {
                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        onAccountDeleted()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Failed to delete account: $error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onUpdateWizardName = { name ->
                viewModel.updateWizardName(name)
            },
            onToggleDarkMode = { enabled ->
                viewModel.toggleDarkMode(enabled)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    aboutTitle: String,
    aboutDescription: String,
    warningTitle: String,
    warningDescription: String,
    onBack: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onUpdateWizardName: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWizardNameDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val padding = (screenWidth * 0.04f).coerceIn(16.dp, 32.dp)
    val spacing = (screenHeight * 0.015f).coerceIn(12.dp, 20.dp)

    if (showWizardNameDialog) {
        var wizardName by remember { mutableStateOf(state.wizardName) }

        AlertDialog(
            onDismissRequest = { showWizardNameDialog = false },
            title = { Text("Update Wizard Name") },
            text = {
                OutlinedTextField(
                    value = wizardName,
                    onValueChange = { wizardName = it },
                    label = { Text("Wizard Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateWizardName(wizardName)
                    showWizardNameDialog = false
                    Toast.makeText(context, "Wizard name updated", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWizardNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirmDelete = { password ->
                onDeleteAccount(password)
                showDeleteAccountDialog = false
            },
            isLoading = isDeleting
        )
    }

    // Error Dialog
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                errorMessage = null
                isDeleting = false
            },
            title = {
                Text(
                    "Delete Account Failed",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    errorMessage = null
                    isDeleting = false
                }) {
                    Text("OK")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = padding, vertical = padding * 0.5f),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Email",
                    subtitle = state.email ?: "Not logged in"
                )

                SettingsActionItem(
                    icon = Icons.Default.Person,
                    title = "Wizard Name",
                    onClick = { showWizardNameDialog = true },
                    subtitle = state.wizardName.ifEmpty { "Set your wizard name" }
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

            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = if (state.darkModeEnabled)
                        painterResource(id = R.drawable.ic_dark_mode)
                    else
                        painterResource(id = R.drawable.ic_light_mode),
                    title = "Dark Mode",
                    subtitle = if (state.darkModeEnabled) "On" else "Off",
                    action = {
                        Switch(
                            checked = state.darkModeEnabled,
                            onCheckedChange = { enabled ->
                                onToggleDarkMode(enabled)
                            }
                        )
                    }
                )
            }


            SettingsSection(title = "Account Management") {
                SettingsActionItem(
                    icon = painterResource(id = R.drawable.ic_delete_account),
                    title = "Delete Account",
                    subtitle = "permanently delete your account and all data",
                    onClick = {
                        android.util.Log.d("SettingsScreen", "Delete account clicked!")
                        showDeleteAccountDialog = true
                    },
                    isDestructive = true
                )
            }

            SettingsSection(title = "About WizardlyDo") {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(padding),
                        verticalArrangement = Arrangement.spacedBy(padding * 0.5f)
                    ) {
                        Text(
                            aboutTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            aboutDescription,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(padding * 0.5f))

                        Text(
                            warningTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            warningDescription,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(padding * 0.75f))

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
fun SettingsScreenPreview() {
    WizardlyDoTheme {
        SettingsContent(
            state = SettingsState(
                email = "wizard@example.com",
                wizardName = "Gandalf",
                darkModeEnabled = true
            ),
            aboutTitle = "WizardlyDo: A Gamified To-Do List App",
            aboutDescription = "WizardlyDo turns your boring task list into an exciting adventure! Complete tasks to gain experience and level up your wizard character.",
            warningTitle = "⚠️ Task Warning System ⚠️",
            warningDescription = "Your wizard takes damage when tasks are overdue. If health reaches zero, you'll need to revive your character! Stay on top of your tasks to keep your wizard healthy and strong.",
            onBack = {},
            onLogoutConfirmed = {},
            onChangePassword = { _, _ -> },
            onDeleteAccount = { _ -> },
            onUpdateWizardName = {},
            onToggleDarkMode = {}
        )
    }
}