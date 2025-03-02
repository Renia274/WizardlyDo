package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.repository.WizardRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.regex.Pattern

data class WizardSignUpState(
    val email: String = "",
    val password: String = "",
    val wizardName: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val isLoading: Boolean = false,
    val isProfileComplete: Boolean = false,
    val error: String? = null
)

class WizardAuthViewModel: ViewModel(), KoinComponent{
    private val auth: FirebaseAuth by inject()
    private val wizardRepository: WizardRepository by inject()

    private val _state = MutableStateFlow(WizardSignUpState())
    val state = _state.asStateFlow()

    val isFormValid: Boolean
        get() = _state.value.wizardName.isNotBlank() &&
                _state.value.email.isNotBlank() &&
                _state.value.password.isNotBlank()

    fun signUpWithEmail() {
        if (!validateForm()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val result = auth.createUserWithEmailAndPassword(
                    _state.value.email,
                    _state.value.password
                ).await()

                result.user?.let { user ->
                    createWizardProfile(
                        userId = user.uid,
                        provider = SignInProvider.EMAIL
                    )
                }
            } catch (e: Exception) {
                handleError("Sign up failed: ${e.message}")
            }
        }
    }

    fun handleGoogleSignIn(credential: AuthCredential) {
        if (!validateForm()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Authenticate with Firebase
                val authResult = auth.signInWithCredential(credential).await()

                // Get user from authentication result
                val user = authResult.user ?: throw Exception("Authentication failed")

                // Check if profile exists
                wizardRepository.getWizardProfile(user.uid).fold(
                    onSuccess = { existingProfile ->
                        if (existingProfile == null) {
                            // Create new profile with collected data
                            val newProfile = WizardProfile(
                                userId = user.uid,
                                wizardClass = _state.value.wizardClass,
                                wizardName = _state.value.wizardName,
                                email = user.email ?: _state.value.email,
                                signInProvider = SignInProvider.GOOGLE
                            )

                            // Save to Repository
                            wizardRepository.createWizardProfile(newProfile).fold(
                                onSuccess = {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        isProfileComplete = true
                                    )
                                },
                                onFailure = { handleError("Profile creation failed: ${it.message}") }
                            )
                        } else {
                            // Existing profile found
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isProfileComplete = true
                            )
                        }
                    },
                    onFailure = { handleError("Profile check failed: ${it.message}") }
                )
            } catch (e: Exception) {
                handleError("Google sign-in failed: ${e.message}")
            }
        }
    }

    private suspend fun createWizardProfile(
        userId: String,
        provider: SignInProvider,
        email: String = _state.value.email
    ) {
        val profile = WizardProfile(
            userId = userId,
            wizardClass = _state.value.wizardClass,
            wizardName = _state.value.wizardName,
            email = email,
            signInProvider = provider
        )

        wizardRepository.createWizardProfile(profile).fold(
            onSuccess = {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isProfileComplete = true
                )
            },
            onFailure = { handleError("Profile creation failed: ${it.message}") }
        )
    }

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (_state.value.wizardName.isBlank()) {
            errors.add("Wizard name required")
        }

        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        if (!emailPattern.matcher(_state.value.email).matches()) {
            errors.add("Invalid email format")
        }

        if (_state.value.password.length !in 8..16) {
            errors.add("Password must be 8-16 characters")
        }

        return if (errors.isNotEmpty()) {
            handleError(errors.joinToString("\n"))
            false
        } else {
            true
        }
    }

    fun handleError(message: String?) {
        _state.value = _state.value.copy(
            error = message,
            isLoading = message != null
        )
    }

    fun updateWizardName(name: String) {
        _state.value = _state.value.copy(wizardName = name)
    }

    fun updateWizardClass(wizardClass: WizardClass) {
        _state.value = _state.value.copy(wizardClass = wizardClass)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }
}