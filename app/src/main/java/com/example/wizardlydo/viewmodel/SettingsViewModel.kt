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
        if (state.value.inAppNotificationsEnabled) {
            activeNotificationFlow.value = notification
            viewModelScope.launch {
                delay(notification.duration)
                clearNotification()
            }
        }
    }

    fun clearNotification() {
        activeNotificationFlow.value = null
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
                        inAppNotificationsEnabled = profile.inAppNotificationsEnabled && hasPermission,
                        damageNotificationsEnabled = profile.damageNotificationsEnabled,
                        emailNotificationsEnabled = profile.emailNotificationsEnabled
                    )
                    notificationPermissionGrantedFlow.value = hasPermission
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