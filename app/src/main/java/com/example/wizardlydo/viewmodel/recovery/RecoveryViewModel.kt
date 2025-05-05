package com.example.wizardlydo.viewmodel.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.models.RecoveryState
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
class RecoveryViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val wizardRepository: WizardRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(RecoveryState())
    val state = mutableState.asStateFlow()

    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    val isEmailValid: Boolean
        get() = state.value.email.isNotBlank() &&
                emailPattern.matcher(state.value.email).matches() &&
                state.value.emailError == null

    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email is required"
            !emailPattern.matcher(email).matches() -> "Invalid email format"
            else -> null
        }

        mutableState.update {
            it.copy(
                email = email,
                emailError = error
            )
        }
    }

    fun sendPasswordResetEmail() {
        updateEmail(state.value.email)
        if (!isEmailValid) return

        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }

            try {
                wizardRepository.findUserByEmail(state.value.email).fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            sendFirebaseResetEmail()
                        } else {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No account found with this email address"
                                )
                            }
                        }
                    },
                    onFailure = { e ->
                        mutableState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error checking account: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun sendFirebaseResetEmail() {
        try {
            firebaseAuth.sendPasswordResetEmail(state.value.email).await()
            mutableState.update {
                it.copy(
                    isLoading = false,
                    isRecoveryEmailSent = true,
                    error = null
                )
            }
        } catch (e: Exception) {
            mutableState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to send recovery email: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    fun resetState() {
        mutableState.update { RecoveryState() }
    }
}