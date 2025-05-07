package com.example.wizardlydo.viewmodel.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.data.models.WizardSignUpState
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.example.wizardlydo.utilities.security.SecurityProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.annotation.KoinViewModel
import java.util.regex.Pattern


@KoinViewModel
class SignupViewModel(
    private val auth: FirebaseAuth,
    private val wizardRepository: WizardRepository,
    private val securityProvider: SecurityProvider
) : ViewModel() {

    private val state = MutableStateFlow(WizardSignUpState())
    val uiState = state.asStateFlow()

    // Validation patterns
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")

    // Validation properties
    val isUsernameValid: Boolean
        get() = state.value.wizardName.isNotBlank() && state.value.usernameError.isNullOrEmpty()

    val isEmailValid: Boolean
        get() = emailPattern.matcher(state.value.email).matches() && state.value.emailError.isNullOrEmpty()

    val isPasswordValid: Boolean
        get() = passwordPattern.matcher(state.value.password).matches() && state.value.passwordError.isNullOrEmpty()

    val isFormValid: Boolean
        get() = isUsernameValid && isEmailValid && isPasswordValid

    private fun encryptPassword(plainPassword: String): String {
        return securityProvider.encrypt(plainPassword)
    }

    fun signUpWithEmail() {
        if (!validateForm()) return

        viewModelScope.launch {
            state.update { it.copy(isLoading = true) }
            try {
                if (checkIfUsernameExists(state.value.wizardName)) {
                    state.update {
                        it.copy(
                            usernameError = "This wizard name is already taken",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(
                    state.value.email,
                    state.value.password
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
            state.update { it.copy(isLoading = true) }
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user ?: throw Exception("Authentication failed")

                if (checkIfUsernameExists(state.value.wizardName)) {
                    state.update {
                        it.copy(
                            usernameError = "This wizard name is already taken",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                wizardRepository.getWizardProfile(user.uid).fold(
                    onSuccess = { existingProfile ->
                        if (existingProfile == null) {
                            val randomPassword = generateSecurePassword()

                            val newProfile = WizardProfile(
                                userId = user.uid,
                                wizardClass = state.value.wizardClass,
                                wizardName = state.value.wizardName,
                                email = user.email ?: state.value.email,
                                passwordHash = encryptPassword(randomPassword),
                                signInProvider = SignInProvider.GOOGLE
                            )

                            wizardRepository.createWizardProfile(newProfile).fold(
                                onSuccess = {
                                    state.update { it.copy(
                                        isLoading = false,
                                        isProfileComplete = true
                                    ) }
                                },
                                onFailure = { handleError("Profile creation failed: ${it.message}") }
                            )
                        } else {
                            state.update { it.copy(
                                isLoading = false,
                                isProfileComplete = true
                            ) }
                        }
                    },
                    onFailure = { handleError("Profile check failed: ${it.message}") }
                )
            } catch (e: Exception) {
                handleError("Google sign-in failed: ${e.message}")
            }
        }
    }

    // Generate secure random password for OAuth users
    private fun generateSecurePassword(length: Int = 16): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + "!@#$%^&*()-_=+[]{}|;:,.<>?/".toList()
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private suspend fun createWizardProfile(
        userId: String,
        provider: SignInProvider,
        email: String = state.value.email
    ) {

        val encryptedPassword = if (provider == SignInProvider.EMAIL) {
            encryptPassword(state.value.password)
        } else {
            encryptPassword(generateSecurePassword())
        }

        val profile = WizardProfile(
            userId = userId,
            wizardClass = state.value.wizardClass,
            wizardName = state.value.wizardName,
            email = email,
            passwordHash = encryptedPassword,
            signInProvider = provider
        )

        wizardRepository.createWizardProfile(profile).fold(
            onSuccess = {
                state.update { it.copy(
                    isLoading = false,
                    isProfileComplete = true
                ) }
            },
            onFailure = { handleError("Profile creation failed: ${it.message}") }
        )
    }

    private suspend fun checkIfUsernameExists(username: String): Boolean {
        return wizardRepository.isWizardNameTaken(username).getOrDefault(false)
    }

    fun validateForm(): Boolean {
        validateWizardName(state.value.wizardName)
        validateEmail(state.value.email)
        validatePassword(state.value.password)
        return isFormValid
    }

    fun validateWizardName(name: String) {
        val error = when {
            name.isBlank() -> "Wizard name required"
            name.length < 3 -> "Wizard name too short"
            name.length > 20 -> "Wizard name too long"
            else -> null
        }
        state.update { it.copy(wizardName = name, usernameError = error) }
    }

    fun validateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email required"
            !emailPattern.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
        state.update { it.copy(email = email, emailError = error) }
    }

    fun validatePassword(password: String) {
        val error = when {
            password.isBlank() -> "Password required"
            password.length < 12 -> "Password must be at least 12 characters"
            !passwordPattern.matcher(password).matches() -> "Must contain uppercase, lowercase, number, and special character"
            else -> null
        }
        state.update { it.copy(password = password, passwordError = error) }
    }

    fun handleError(message: String?) {
        state.update { it.copy(error = message, isLoading = false) }
    }

    fun updateWizardName(name: String) = validateWizardName(name)
    fun updateWizardClass(wizardClass: WizardClass) {
        state.update { it.copy(wizardClass = wizardClass) }
    }
    fun updateEmail(email: String) = validateEmail(email)
    fun updatePassword(password: String) = validatePassword(password)

    fun togglePasswordVisibility() {
        state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }
}