package com.example.wizardlydo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.wizardlydo.data.models.SettingsState
import com.example.wizardlydo.repository.wizard.WizardRepository
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