package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.repository.WizardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginViewModel : ViewModel(), KoinComponent {
    private val auth: FirebaseAuth by inject()
    private val wizardRepository: WizardRepository by inject()

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    val isFormValid: Boolean
        get() = _state.value.email.isNotBlank() &&
                _state.value.password.isNotBlank()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun login() {
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

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)