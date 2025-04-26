package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.example.wizardlydo.utilities.NotificationWorker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.concurrent.TimeUnit

@KoinViewModel
class SettingsViewModel(
    private val wizardRepository: WizardRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val stateFlow = MutableStateFlow(
        SettingsState(
            email = auth.currentUser?.email,
            reminderEnabled = true,
            reminderDays = 1,
            pushNotificationsEnabled = true,
            damageNotificationsEnabled = true
        )
    )
    val state: StateFlow<SettingsState> = stateFlow.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    stateFlow.value = stateFlow.value.copy(
                        email = auth.currentUser?.email,
                        reminderEnabled = profile.reminderEnabled,
                        reminderDays = profile.reminderDays,
                        pushNotificationsEnabled = profile.pushNotificationsEnabled,
                        damageNotificationsEnabled = profile.damageNotificationsEnabled
                    )
                }
            }
        }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { currentProfile ->
                    val updatedProfile = currentProfile.copy(
                        reminderEnabled = enabled
                    )
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(reminderEnabled = enabled)
                        if (enabled) scheduleNotificationWorker()
                    }
                }
            }
        }
    }

    fun updateReminderDays(days: Int) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { currentProfile ->
                    val updatedProfile = currentProfile.copy(
                        reminderDays = days
                    )
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(reminderDays = days)
                    }
                }
            }
        }
    }

    fun updatePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { currentProfile ->
                    val updatedProfile = currentProfile.copy(
                        pushNotificationsEnabled = enabled
                    )
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(pushNotificationsEnabled = enabled)
                    }
                }
            }
        }
    }

    fun updateDamageNotifications(enabled: Boolean) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { currentProfile ->
                    val updatedProfile = currentProfile.copy(
                        damageNotificationsEnabled = enabled
                    )
                    wizardRepository.updateWizardProfile(userId, updatedProfile).onSuccess {
                        stateFlow.value = stateFlow.value.copy(damageNotificationsEnabled = enabled)
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

        // If current password is provided, we need to re-authenticate
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
            // Direct password update (may require recent login)
            updatePassword(user, newPassword, onSuccess, onError)
        }
    }

    private fun updatePassword(
        user: FirebaseUser,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        user.updatePassword(newPassword).addOnCompleteListener { updateTask: Task<Void> ->
            if (updateTask.isSuccessful) {
                updatePasswordInRepository(newPassword)
                onSuccess()
            } else {
                onError(updateTask.exception?.message ?: "Password update failed")
            }
        }
    }

    private fun updatePasswordInRepository(newPassword: String) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId: String ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { currentProfile ->
                    val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
                    val updatedProfile = currentProfile.copy(passwordHash = hashedPassword)
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

    fun logout() {
        auth.signOut()
    }
}