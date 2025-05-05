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

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val DARK_MODE_KEY = "dark_mode"

    private val stateFlow = MutableStateFlow(
        SettingsState(
            email = auth.currentUser?.email,
            wizardName = "",
            darkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false)
        )
    )
    val state: StateFlow<SettingsState> = stateFlow.asStateFlow()

    private val aboutTitleFlow = MutableStateFlow("")
    val aboutTitle: StateFlow<String> = aboutTitleFlow.asStateFlow()

    private val aboutDescriptionFlow = MutableStateFlow("")
    val aboutDescription: StateFlow<String> = aboutDescriptionFlow.asStateFlow()

    private val warningTitleFlow = MutableStateFlow("")
    val warningTitle: StateFlow<String> = warningTitleFlow.asStateFlow()

    private val warningDescriptionFlow = MutableStateFlow("")
    val warningDescription: StateFlow<String> = warningDescriptionFlow.asStateFlow()

    init {
        loadSettings()
        loadAboutText()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    // Save dark mode from profile to shared preferences
                    profile.darkModeEnabled.let { darkMode ->
                        sharedPreferences.edit().putBoolean(DARK_MODE_KEY, darkMode).apply()
                    }

                    stateFlow.value = stateFlow.value.copy(
                        email = auth.currentUser?.email,
                        wizardName = profile.wizardName,
                        darkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false)
                    )
                }
            }
        }
    }

    private fun loadAboutText() {
        viewModelScope.launch {
            try {
                val inputStream = context.assets.open("about_wizardlydo.txt")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                val content = String(buffer, Charsets.UTF_8)
                val lines = content.split("\n")

                if (lines.size >= 4) {
                    aboutTitleFlow.value = lines[0]
                    aboutDescriptionFlow.value = lines[1]
                    warningTitleFlow.value = lines[2]
                    warningDescriptionFlow.value = lines[3]
                }
            } catch (e: Exception) {
                // Set default values if the file can't be read
                aboutTitleFlow.value = "WizardlyDo: A Gamified To-Do List App"
                aboutDescriptionFlow.value = "WizardlyDo turns your boring task list into an exciting adventure! Complete tasks to gain experience and level up your wizard character."
                warningTitleFlow.value = "⚠️ Task Warning System ⚠️"
                warningDescriptionFlow.value = "Your wizard takes damage when tasks are overdue. If health reaches zero, you'll need to revive your character! Stay on top of your tasks to keep your wizard healthy and strong."
            }
        }
    }

    fun updateWizardName(name: String) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(wizardName = name)
                    wizardRepository.updateWizardProfile(userId, updatedProfile)
                    stateFlow.value = stateFlow.value.copy(wizardName = name)
                }
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        // Save to preferences
        sharedPreferences.edit().putBoolean(DARK_MODE_KEY, enabled).apply()

        // Update local state
        stateFlow.value = stateFlow.value.copy(darkModeEnabled = enabled)

        // Also update profile
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(darkModeEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile)
                }
            }
        }

        // No need to recreate activity here - the listener will handle it
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