package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.repository.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val wizardRepository: WizardRepository
) : ViewModel() {
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
                val result = wizardRepository.getWizardProfile(
                    _state.value.email
                )

                result.fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                loginSuccess = true
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "User not found"
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
                    error = "An unexpected error occurred"
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