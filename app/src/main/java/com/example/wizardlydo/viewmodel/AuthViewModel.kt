package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.models.WizardSignUpState
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



class WizardAuthViewModel: ViewModel(), KoinComponent {
    private val auth: FirebaseAuth by inject()
    private val wizardRepository: WizardRepository by inject()

    private val _state = MutableStateFlow(WizardSignUpState())
    val state = _state.asStateFlow()

    // Validation properties
    val isUsernameValid: Boolean
        get() = _state.value.wizardName.isNotBlank() && !_state.value.usernameError.isNullOrEmpty().not()

    val isEmailValid: Boolean
        get() = emailPattern.matcher(_state.value.email).matches() && !_state.value.emailError.isNullOrEmpty().not()

    val isPasswordValid: Boolean
        get() = passwordPattern.matcher(_state.value.password).matches() && !_state.value.passwordError.isNullOrEmpty().not()

    val isFormValid: Boolean
        get() = isUsernameValid && isEmailValid && isPasswordValid

    // Validation patterns
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")



    fun signUpWithEmail() {
        if (!validateForm()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Check if username is already taken
                val isUsernameTaken = checkIfUsernameExists(_state.value.wizardName)
                if (isUsernameTaken) {
                    _state.value = _state.value.copy(
                        usernameError = "This wizard name is already taken",
                        isLoading = false
                    )
                    return@launch
                }

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

    private suspend fun checkIfUsernameExists(username: String): Boolean {
        // Implement this method to check if the username already exists in the database
        return wizardRepository.isWizardNameTaken(username).getOrDefault(false)
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

                // Check if username is already taken
                val isUsernameTaken = checkIfUsernameExists(_state.value.wizardName)
                if (isUsernameTaken) {
                    _state.value = _state.value.copy(
                        usernameError = "This wizard name is already taken",
                        isLoading = false
                    )
                    return@launch
                }

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
        // Validate all fields
        validateWizardName(_state.value.wizardName)
        validateEmail(_state.value.email)
        validatePassword(_state.value.password)

        // Check if any field has validation errors
        return isFormValid
    }

    fun validateWizardName(name: String) {
        val error = when {
            name.isBlank() -> "Wizard name required"
            name.length < 3 -> "Wizard name too short"
            name.length > 20 -> "Wizard name too long"
            else -> null
        }

        _state.value = _state.value.copy(
            wizardName = name,
            usernameError = error
        )
    }

    fun validateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email required"
            !emailPattern.matcher(email).matches() -> "Invalid email format"
            else -> null
        }

        _state.value = _state.value.copy(
            email = email,
            emailError = error
        )
    }

    fun validatePassword(password: String) {
        val error = when {
            password.isBlank() -> "Password required"
            password.length < 12 -> "Password must be at least 8 characters"
            !passwordPattern.matcher(password).matches() -> "Password must contain uppercase, lowercase, number, and special character"
            else -> null
        }

        _state.value = _state.value.copy(
            password = password,
            passwordError = error
        )
    }

    fun handleError(message: String?) {
        _state.value = _state.value.copy(
            error = message,
            isLoading = false
        )
    }

    fun updateWizardName(name: String) {
        validateWizardName(name)
    }

    fun updateWizardClass(wizardClass: WizardClass) {
        _state.value = _state.value.copy(wizardClass = wizardClass)
    }

    fun updateEmail(email: String) {
        validateEmail(email)
    }

    fun updatePassword(password: String) {
        validatePassword(password)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }
}