package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.models.LoginState
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.annotation.KoinViewModel
import java.util.regex.Pattern

@KoinViewModel
class LoginViewModel(
    private val auth: FirebaseAuth,
    private val wizardRepository: WizardRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginState())
    val state = mutableState.asStateFlow()

    // Email validation pattern
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    val isFormValid: Boolean
        get() = state.value.emailError == null &&
                state.value.passwordError == null &&
                state.value.email.isNotBlank() &&
                state.value.password.isNotBlank()

    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email is required"
            !emailPattern.matcher(email).matches() -> "Invalid email format"
            else -> null
        }

        mutableState.update { it.copy(email = email, emailError = error) }
    }

    // Password pattern requiring at least 8 chars, 1 uppercase, 1 lowercase, 1 number, and 1 special character
    private val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")

    fun updatePassword(password: String) {
        val error = when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !passwordPattern.matcher(password).matches() -> "Password must contain uppercase, lowercase, number, and special character"
            else -> null
        }

        mutableState.update { it.copy(password = password, passwordError = error) }
    }

    fun togglePasswordVisibility() {
        mutableState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        // Validate fields before attempting login
        updateEmail(state.value.email)
        updatePassword(state.value.password)

        if (!isFormValid) return

        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }

            try {
                val authResult = auth.signInWithEmailAndPassword(
                    state.value.email,
                    state.value.password
                ).await()

                val userId = authResult.user?.uid
                    ?: throw Exception("Authentication failed")

                wizardRepository.getWizardProfile(userId).fold(
                    onSuccess = { profile ->
                        mutableState.update {
                            if (profile != null) {
                                it.copy(isLoading = false, loginSuccess = true)
                            } else {
                                it.copy(
                                    isLoading = false,
                                    error = "Profile not found. Please complete registration."
                                )
                            }
                        }
                    },
                    onFailure = { e ->
                        mutableState.update {
                            it.copy(
                                isLoading = false,
                                error = "Login failed: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        error = "Authentication failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
}
