package com.example.wizardlydo.viewmodel.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.models.PinSetupState
import com.example.wizardlydo.repository.pin.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class PinViewModel(
    private val pinRepository: PinRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(PinSetupState())
    val state = mutableState.asStateFlow()

    fun updatePin(pin: String) {
        mutableState.update {
            it.copy(
                pin = pin,
                error = null
            )
        }
    }

    fun validateAndSavePin() {
        viewModelScope.launch {
            val currentPin = state.value.pin

            if (currentPin.length != 4 || !currentPin.all { it.isDigit() }) {
                mutableState.update {
                    it.copy(
                        error = "PIN must be exactly 4 digits"
                    )
                }
                return@launch
            }

            pinRepository.savePin(currentPin).fold(
                onSuccess = {
                    mutableState.update {
                        it.copy(
                            isPinSaved = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    mutableState.update {
                        it.copy(
                            error = "Failed to save PIN: ${e.message}",
                            isPinSaved = false
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    fun verifyPin() {
        viewModelScope.launch {
            val currentPin = state.value.pin

            if (currentPin.length != 4 || !currentPin.all { it.isDigit() }) {
                mutableState.update {
                    it.copy(
                        error = "PIN must be exactly 4 digits"
                    )
                }
                return@launch
            }

            pinRepository.validatePin(currentPin).fold(
                onSuccess = { isValid ->
                    mutableState.update {
                        if (isValid) {
                            it.copy(
                                isPinVerified = true,
                                error = null
                            )
                        } else {
                            it.copy(
                                error = "Incorrect PIN",
                                isPinVerified = false
                            )
                        }
                    }
                },
                onFailure = { e ->
                    mutableState.update {
                        it.copy(
                            error = "PIN verification failed: ${e.message}",
                            isPinVerified = false
                        )
                    }
                }
            )
        }
    }
}