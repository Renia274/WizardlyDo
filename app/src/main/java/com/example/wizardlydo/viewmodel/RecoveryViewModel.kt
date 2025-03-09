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

class RecoveryViewModel : ViewModel(), KoinComponent {
    private val firebaseAuth: FirebaseAuth by inject()
    private val wizardRepository: WizardRepository by inject()

    private val _state = MutableStateFlow(RecoveryState())
    val state = _state.asStateFlow()

    val isEmailValid: Boolean
        get() = _state.value.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun sendPasswordResetEmail() {
        if (!isEmailValid) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // First check if user exists in our database using repository
                wizardRepository.findUserByEmail(_state.value.email).fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            // User exists, send password reset email
                            try {
                                firebaseAuth.sendPasswordResetEmail(_state.value.email).await()
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    isRecoveryEmailSent = true,
                                    error = null
                                )
                            } catch (e: Exception) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Failed to send recovery email: ${e.message}"
                                )
                            }
                        } else {
                            // No user found with this email
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "No account found with this email address"
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Error checking account: ${e.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = RecoveryState()
    }
}

data class RecoveryState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isRecoveryEmailSent: Boolean = false,
    val error: String? = null
)