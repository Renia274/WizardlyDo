package com.example.wizardlydo.viewmodel

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.repository.pin.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent


class PinViewModel(
    private val pinRepository: PinRepository
) : ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(PinSetupState())
    val state = _state.asStateFlow()

    fun updatePin(pin: String) {
        _state.value = _state.value.copy(
            pin = pin,
            error = null
        )
    }



    fun validateAndSavePin() {
        viewModelScope.launch {
            val pin = _state.value.pin

            // Validate pin is exactly 4 digits
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                _state.value = _state.value.copy(
                    error = "PIN must be exactly 4 digits"
                )
                return@launch
            }

            // Save PIN with biometrics preference
            val result = pinRepository.savePin(pin)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isPinSaved = true,
                        error = null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        error = "Failed to save PIN: ${e.message}",
                        isPinSaved = false
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun isBiometricsAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun verifyPin() {
        viewModelScope.launch {
            val pin = _state.value.pin

            // Validate pin is exactly 4 digits
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                _state.value = _state.value.copy(
                    error = "PIN must be exactly 4 digits"
                )
                return@launch
            }

            // Verify PIN
            val result = pinRepository.validatePin(pin)

            result.fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        _state.value = _state.value.copy(
                            isPinSaved = true,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            error = "Incorrect PIN",
                            isPinSaved = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        error = "PIN verification failed: ${e.message}",
                        isPinSaved = false
                    )
                }
            )
        }
    }



    data class PinSetupState(
        val pin: String = "",
        val isPinSaved: Boolean = false,
        val error: String? = null,
        val isLoading: Boolean = false
    )
}