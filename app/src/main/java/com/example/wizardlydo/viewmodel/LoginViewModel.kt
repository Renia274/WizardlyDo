package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.models.LoginState
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.regex.Pattern

class LoginViewModel : ViewModel(), KoinComponent {
    private val auth: FirebaseAuth by inject()
    private val wizardRepository: WizardRepository by inject()

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    // Email validation pattern
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    val isFormValid: Boolean
    get() = _state.value.emailError == null &&
            _state.value.passwordError == null &&
            _state.value.email.isNotBlank() &&
            _state.value.password.isNotBlank()

    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email is required"
            !emailPattern.matcher(email).matches() -> "Invalid email format"
            else -> null
        }

        _state.value = _state.value.copy(
            email = email,
            emailError = error
        )
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

        _state.value = _state.value.copy(
            password = password,
            passwordError = error
        )
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }

    fun login() {
        // Validate fields before attempting login
        updateEmail(_state.value.email)
        updatePassword(_state.value.password)

        // Check if form is valid after validation
        if (!isFormValid) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // Authenticate with Firebase
                val authResult = auth.signInWithEmailAndPassword(
                    _state.value.email,
                    _state.value.password
                ).await()

                // Get current user ID from Firebase Auth
                val userId = authResult.user?.uid
                    ?: throw Exception("Authentication failed")

                // fetch the wizard profile using the authenticated user ID
                wizardRepository.getWizardProfile(userId).fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                loginSuccess = true
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Profile not found. Please complete registration."
                            )
                        }
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Login failed: ${it.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

