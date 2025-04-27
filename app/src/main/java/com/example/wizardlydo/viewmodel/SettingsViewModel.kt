package com.example.wizardlydo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.wizardlydo.comps.NotificationType
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.repository.tasks.TaskRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.example.wizardlydo.utilities.EmailSender
import com.example.wizardlydo.utilities.NotificationPermissionHandler
import com.example.wizardlydo.utilities.NotificationWorker
import com.example.wizardlydo.utilities.WizardEmailTemplates
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.concurrent.TimeUnit


@KoinViewModel
class SettingsViewModel(
    private val wizardRepository: WizardRepository,
    private val taskRepository: TaskRepository,
    private val workManager: WorkManager,
    private val context: Context
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val emailSender = EmailSender(context)

    // State for settings
    private val stateFlow = MutableStateFlow(
        SettingsState(
            email = auth.currentUser?.email,
            reminderEnabled = true,
            reminderDays = 1,
            inAppNotificationsEnabled = false,
            damageNotificationsEnabled = false,
            emailNotificationsEnabled = false
        )
    )
    val state: StateFlow<SettingsState> = stateFlow.asStateFlow()

    // Notification permission state
    private val notificationPermissionGrantedFlow = MutableStateFlow(
        NotificationPermissionHandler.checkPermission(context)
    )
    val notificationPermissionGranted: StateFlow<Boolean> = notificationPermissionGrantedFlow.asStateFlow()

    init {
        loadSettings()
    }

    // Make this public so it can be accessed directly from TaskScreen
    val activeNotificationFlow = MutableStateFlow<InAppNotificationData?>(null)
    val activeNotification: StateFlow<InAppNotificationData?> = activeNotificationFlow.asStateFlow()

    open class InAppNotificationData(
        val message: String,
        val type: NotificationType,
        val duration: Long = 3000
    ) {
        class Info(message: String, duration: Long = 3000) :
            InAppNotificationData(message, NotificationType.INFO, duration)

        class Warning(message: String, duration: Long = 4000) :
            InAppNotificationData(message, NotificationType.WARNING, duration)
    }

    fun showNotification(notification: InAppNotificationData) {
        // Remove the conditional check so notifications are shown regardless of settings
        // This makes testing easier
        activeNotificationFlow.value = notification
        viewModelScope.launch {
            delay(notification.duration)
            clearNotification()
        }
    }

    fun clearNotification() {
        activeNotificationFlow.value = null
    }

    fun checkNotificationPermission() {
        notificationPermissionGrantedFlow.value = NotificationPermissionHandler.checkPermission(context)
    }

    private fun loadSettings() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val hasPermission = NotificationPermissionHandler.checkPermission(context)
                    stateFlow.value = stateFlow.value.copy(
                        email = auth.currentUser?.email,
                        reminderEnabled = profile.reminderEnabled,
                        reminderDays = profile.reminderDays,
                        inAppNotificationsEnabled = profile.inAppNotificationsEnabled,
                        damageNotificationsEnabled = profile.damageNotificationsEnabled,
                        emailNotificationsEnabled = profile.emailNotificationsEnabled
                    )
                    notificationPermissionGrantedFlow.value = hasPermission
                }
            }
        }
    }

    /**
     * Checks for tasks that are due according to reminder settings and triggers notifications immediately.
     * This is useful for testing the notification system.
     */
    fun checkDueTasksImmediately() {
        viewModelScope.launch {
            try {
                // Get the current user ID
                val userId = auth.currentUser?.uid ?: return@launch

                // Get current reminder settings
                val reminderDays = stateFlow.value.reminderDays

                // Get current date
                val today = System.currentTimeMillis()
                val oneDayInMillis = 24 * 60 * 60 * 1000L

                // Calculate the threshold date based on reminder settings
                // Tasks due within this many days will trigger a notification
                val thresholdDate = today + (reminderDays * oneDayInMillis)

                // Use getUpcomingTasks from TaskRepository
                val dueTasks = taskRepository.getUpcomingTasks(userId, thresholdDate)

                if (dueTasks.isEmpty()) {
                    // No due tasks found
                    showNotification(InAppNotificationData.Info("No upcoming tasks due within $reminderDays day(s)"))
                    return@launch
                }

                // Show notification for testing
                val taskCount = dueTasks.size
                val dueTasksList = dueTasks.take(3).joinToString(", ") { it.title }
                val moreTasksText = if (taskCount > 3) " and ${taskCount - 3} more" else ""

                showNotification(
                    InAppNotificationData.Warning(
                        "REMINDER: $taskCount task(s) due soon: $dueTasksList$moreTasksText",
                        duration = 6000
                    )
                )

                // Also send a test email if email notifications are enabled
                if (stateFlow.value.emailNotificationsEnabled) {
                    val userEmail = stateFlow.value.email ?: return@launch

                    emailSender.sendEmail(
                        userEmail,
                        "🧙‍♂️ Task Reminder from WizardlyDo",
                        WizardEmailTemplates.getTaskReminderTemplate(
                            taskNames = dueTasks.take(5).map { it.title },
                            daysUntilDue = reminderDays
                        )
                    )
                }
            } catch (e: Exception) {
                showNotification(InAppNotificationData.Warning("Error checking due tasks: ${e.message}"))
            }
        }
    }

    fun updateInAppNotifications(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(inAppNotificationsEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(inAppNotificationsEnabled = enabled)
                        if (enabled) {
                            showNotification(InAppNotificationData.Info("In-app notifications enabled"))
                        }
                    }
                }
            }
        }
    }

    fun updateReminderDays(days: Int) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(reminderDays = days)
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(reminderDays = days)
                        showNotification(InAppNotificationData.Info("Reminder days set to $days"))
                    }
                }
            }
        }
    }

    fun updateDamageNotifications(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(damageNotificationsEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(damageNotificationsEnabled = enabled)
                        val message = if (enabled) "Damage alerts enabled" else "Damage alerts disabled"
                        showNotification(InAppNotificationData.Info(message))
                    }
                }
            }
        }
    }

    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(emailNotificationsEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(emailNotificationsEnabled = enabled)
                        val message = if (enabled) "Email notifications enabled" else "Email notifications disabled"
                        showNotification(InAppNotificationData.Info(message))
                    }
                }
            }
        }
    }

    fun changePassword(
        newPassword: String,
        currentPassword: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = auth.currentUser ?: run {
            onError("User not authenticated")
            return
        }

        if (currentPassword.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updatePassword(user, newPassword, onSuccess, onError)
                } else {
                    onError(task.exception?.message ?: "Authentication failed")
                }
            }
        } else {
            updatePassword(user, newPassword, onSuccess, onError)
        }
    }

    private fun updatePassword(
        user: FirebaseUser,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        user.updatePassword(newPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updatePasswordInRepository(newPassword)
                showNotification(InAppNotificationData.Info("Password updated successfully"))
                onSuccess()
            } else {
                onError(task.exception?.message ?: "Password update failed")
            }
        }
    }

    private fun updatePasswordInRepository(newPassword: String) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
                    val updatedProfile = profile.copy(passwordHash = hashedPassword)
                    wizardRepository.updateWizardProfile(userId, updatedProfile)
                }
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workerRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints).build()

        workManager.enqueueUniquePeriodicWork(
            "taskReminderWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workerRequest
        )
    }

    fun sendDamagePreviewEmail() {
        val userEmail = stateFlow.value.email ?: return

        emailSender.sendEmail(
            userEmail,
            "🧙‍♂️ Your Wizard Has Taken Damage!",
            WizardEmailTemplates.getDamageEmailTemplate(
                damage = 20,
                currentHealth = 80,
                maxHealth = 100,
                tasks = listOf("Complete project report", "Organize team meeting")
            )
        )
    }

    fun sendCriticalPreviewEmail() {
        val userEmail = stateFlow.value.email ?: return

        emailSender.sendEmail(
            userEmail,
            "⚠️ URGENT: Your Wizard Is In Critical Danger!",
            WizardEmailTemplates.getCriticalHealthTemplate(
                damage = 30,
                currentHealth = 15,
                maxHealth = 100,
                tasks = listOf("Complete project report", "Organize team meeting", "Call client")
            )
        )
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(reminderEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(reminderEnabled = enabled)
                        if (enabled) scheduleNotificationWorker()
                    }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}