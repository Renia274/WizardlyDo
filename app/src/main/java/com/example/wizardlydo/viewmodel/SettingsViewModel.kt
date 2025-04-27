package com.example.wizardlydo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.wizardlydo.comps.NotificationType
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.repository.wizard.WizardRepository
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


@KoinViewModel
class SettingsViewModel(
    private val wizardRepository: WizardRepository,
    private val context: Context
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth


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
                    stateFlow.value = stateFlow.value.copy(
                        email = auth.currentUser?.email,
                        reminderEnabled = profile.reminderEnabled,
                        reminderDays = profile.reminderDays,
                        damageNotificationsEnabled = profile.damageNotificationsEnabled,
                        emailNotificationsEnabled = profile.emailNotificationsEnabled
                    )
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





    fun logout() {
        auth.signOut()
    }
}