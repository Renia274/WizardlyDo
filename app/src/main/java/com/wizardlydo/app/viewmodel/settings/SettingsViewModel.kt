package com.wizardlydo.app.viewmodel.settings

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wizardlydo.app.data.models.SettingsState
import com.wizardlydo.app.repository.wizard.WizardRepository
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
            email = null, // Start with null, will be loaded from wizard profile
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

        // Add auth state listener to handle late Firebase Auth restoration
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null && stateFlow.value.email == null) {
                // Auth state restored, reload settings
                loadSettings()
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                wizardRepository.getWizardProfile(currentUser.uid).fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            // Save dark mode from profile to shared preferences
                            profile.darkModeEnabled.let { darkMode ->
                                sharedPreferences.edit { putBoolean(DARK_MODE_KEY, darkMode) }
                            }

                            stateFlow.value = stateFlow.value.copy(
                                email = profile.email, // Use email from wizard profile
                                wizardName = profile.wizardName,
                                darkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false)
                            )
                        } else {
                            // Profile not found, use Firebase Auth email as fallback
                            stateFlow.value = stateFlow.value.copy(
                                email = currentUser.email ?: "No email found"
                            )
                        }
                    },
                    onFailure = {
                        // Repository failed, use Firebase Auth email as fallback
                        stateFlow.value = stateFlow.value.copy(
                            email = currentUser.email ?: "No email found"
                        )
                    }
                )
            } else {
                // No current user
                stateFlow.value = stateFlow.value.copy(
                    email = "Not logged in"
                )
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
        sharedPreferences.edit { putBoolean(DARK_MODE_KEY, enabled) }

        // Update local state
        stateFlow.value = stateFlow.value.copy(darkModeEnabled = enabled)

        // update wizard profile
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                wizardRepository.getWizardProfile(userId).getOrNull()?.let { profile ->
                    val updatedProfile = profile.copy(darkModeEnabled = enabled)
                    wizardRepository.updateWizardProfile(userId, updatedProfile)
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

    fun deleteAccount(
        currentPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = auth.currentUser ?: run {
            onError("User not authenticated")
            return
        }

        val email = user.email ?: run {
            onError("User email not found")
            return
        }

        if (currentPassword.isBlank()) {
            onError("Password is required")
            return
        }

        // Re-authenticate user before deletion
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                viewModelScope.launch {
                    try {
                        // Delete wizard profile and all associated data
                        val deleteResult = wizardRepository.deleteWizardProfile(user.uid)

                        if (deleteResult.isSuccess) {
                            // Clear shared preferences
                            sharedPreferences.edit { clear() }

                            // Delete Firebase Auth user
                            user.delete().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    val errorMessage = when {
                                        deleteTask.exception?.message?.contains("network") == true ->
                                            "Network error occurred"
                                        deleteTask.exception?.message?.contains("too-many-requests") == true ->
                                            "Too many requests. Please try again later"
                                        else -> deleteTask.exception?.message ?: "Failed to delete account"
                                    }
                                    onError(errorMessage)
                                }
                            }
                        } else {
                            onError("Failed to delete user data from local storage")
                        }
                    } catch (e: Exception) {
                        onError("Failed to delete user data: ${e.localizedMessage}")
                    }
                }
            } else {
                val errorMessage = when {
                    authTask.exception?.message?.contains("password") == true ->
                        "Incorrect password"
                    authTask.exception?.message?.contains("network") == true ->
                        "Network error. Please check your connection"
                    authTask.exception?.message?.contains("too-many-requests") == true ->
                        "Too many attempts. Please try again later"
                    else -> authTask.exception?.message ?: "Authentication failed"
                }
                onError(errorMessage)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
    }
}