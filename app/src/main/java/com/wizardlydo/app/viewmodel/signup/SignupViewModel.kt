package com.wizardlydo.app.viewmodel.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wizardlydo.app.data.models.WizardSignUpState
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.providers.SignInProvider
import com.wizardlydo.app.repository.wizard.WizardRepository
import com.wizardlydo.app.utilities.RememberMeManager
import com.wizardlydo.app.utilities.security.SecurityProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val securityProvider: SecurityProvider,
    private val rememberMeManager: RememberMeManager
) : ViewModel() {

    private val state = MutableStateFlow(WizardSignUpState(isCheckingUsername = true))
    val uiState = state.asStateFlow()

    private var usernameCheckJob: Job? = null

    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")

    val isUsernameValid: Boolean
        get() = state.value.wizardName.isNotBlank() &&
                state.value.usernameError.isNullOrEmpty() &&
                !state.value.isCheckingUsername

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
                // Double-check username availability before proceeding
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
                // Auto-save email for future logins
                rememberMeManager.saveEmail(email)
                rememberMeManager.setRememberMe(true)

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


    private fun checkUsernameAvailability(username: String) {
        // Cancel previous check
        usernameCheckJob?.cancel()

        usernameCheckJob = viewModelScope.launch {
            delay(500)
            if (username.length >= 3 && username.length <= 20) {
                state.update { it.copy(isCheckingUsername = true) }

                try {
                    val isTaken = checkIfUsernameExists(username)
                    state.update { currentState ->
                        currentState.copy(
                            usernameError = if (isTaken) "This wizard name is already taken" else null,
                            isCheckingUsername = false
                        )
                    }
                } catch (e: Exception) {
                    state.update { it.copy(isCheckingUsername = false) }
                }
            }
        }
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

        // Only check availability if basic validation passes
        if (error == null) {
            checkUsernameAvailability(name)
        } else {
            // Cancel any pending username check if validation fails
            usernameCheckJob?.cancel()
        }
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

    override fun onCleared() {
        super.onCleared()
        usernameCheckJob?.cancel()
    }
}