package com.wizardlydo.app.viewmodel.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wizardlydo.app.models.WizardSignUpState
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

    private val state = MutableStateFlow(WizardSignUpState(isCheckingUsername = false))
    val uiState = state.asStateFlow()

    private var usernameCheckJob: Job? = null

    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")

    // Wizard-themed suffixes and prefixes
    private val wizardPrefixes = listOf(
        "Dark", "Mystic", "Ancient", "Elder", "Grand", "Wise", "Shadow",
        "Arcane", "Star", "Moon", "Sun", "Storm", "Frost", "Fire"
    )

    private val wizardSuffixes = listOf(
        "Sage", "Mage", "Sorcerer", "Wizard", "Enchanter", "Keeper",
        "Walker", "Weaver", "Binder", "Caller", "Master", "Seeker"
    )

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

    private suspend fun generateUsernameSuggestions(baseName: String): List<String> {
        val suggestions = mutableSetOf<String>() // Use Set to avoid duplicates
        val cleanBaseName = baseName.trim()

        try {
            // Strategy 1: Add numbers (1-99)
            var numberAttempts = 0
            var currentNumber = 1
            while (suggestions.size < 3 && numberAttempts < 50) {
                val suggestion = "$cleanBaseName$currentNumber"
                if (suggestion.length <= 20) {
                    val isTaken = checkIfUsernameExists(suggestion)
                    if (!isTaken) {
                        suggestions.add(suggestion)
                    }
                }
                currentNumber++
                numberAttempts++
            }

            // Strategy 2: Add wizard-themed prefixes
            if (suggestions.size < 3) {
                for (prefix in wizardPrefixes.shuffled()) {
                    if (suggestions.size >= 3) break
                    val suggestion = "$prefix$cleanBaseName"
                    if (suggestion.length <= 20) {
                        val isTaken = checkIfUsernameExists(suggestion)
                        if (!isTaken) {
                            suggestions.add(suggestion)
                        }
                    }
                }
            }

            // Strategy 3: Add wizard-themed suffixes
            if (suggestions.size < 3) {
                for (suffix in wizardSuffixes.shuffled()) {
                    if (suggestions.size >= 3) break
                    val suggestion = "$cleanBaseName$suffix"
                    if (suggestion.length <= 20) {
                        val isTaken = checkIfUsernameExists(suggestion)
                        if (!isTaken) {
                            suggestions.add(suggestion)
                        }
                    }
                }
            }

            // Strategy 4: Combine prefix + base + number (if still needed)
            if (suggestions.size < 3) {
                for (prefix in wizardPrefixes.shuffled().take(5)) {
                    if (suggestions.size >= 3) break
                    for (num in 1..20) {
                        if (suggestions.size >= 3) break
                        val suggestion = "$prefix$cleanBaseName$num"
                        if (suggestion.length <= 20) {
                            val isTaken = checkIfUsernameExists(suggestion)
                            if (!isTaken) {
                                suggestions.add(suggestion)
                            }
                        }
                    }
                }
            }

            // Strategy 5: Just use random numbers if nothing works
            if (suggestions.size < 3) {
                for (num in 100..9999) {
                    if (suggestions.size >= 3) break
                    val suggestion = "$cleanBaseName$num"
                    if (suggestion.length <= 20) {
                        val isTaken = checkIfUsernameExists(suggestion)
                        if (!isTaken) {
                            suggestions.add(suggestion)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            // If something fails, return what we have
            return suggestions.take(3)
        }

        return suggestions.take(3)
    }

    fun signUpWithEmail() {
        if (!validateForm()) return

        viewModelScope.launch {
            state.update { it.copy(isLoading = true) }
            try {
                // Double-check username availability before proceeding
                if (checkIfUsernameExists(state.value.wizardName)) {
                    val suggestions = generateUsernameSuggestions(state.value.wizardName)
                    state.update {
                        it.copy(
                            usernameError = "This wizard name is already taken",
                            usernameSuggestions = suggestions,
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
            delay(500) // Debounce

            if (username.length in 3..20) {
                state.update { it.copy(isCheckingUsername = true) }

                try {
                    val isTaken = checkIfUsernameExists(username)

                    if (isTaken) {
                        // Generate suggestions when username is taken
                        val suggestions = generateUsernameSuggestions(username)
                        state.update { currentState ->
                            currentState.copy(
                                usernameError = "This wizard name is already taken",
                                usernameSuggestions = suggestions,
                                isCheckingUsername = false
                            )
                        }
                    } else {
                        // Username is available
                        state.update { currentState ->
                            currentState.copy(
                                usernameError = null,
                                usernameSuggestions = emptyList(),
                                isCheckingUsername = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Error during check
                    state.update {
                        it.copy(
                            isCheckingUsername = false,
                            usernameError = "Could not check availability"
                        )
                    }
                }
            } else {
                // Clear suggestions if username doesn't meet length requirements
                state.update {
                    it.copy(
                        usernameSuggestions = emptyList()
                    )
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

        state.update { it.copy(
            wizardName = name,
            usernameError = error,
            usernameSuggestions = emptyList() // Clear suggestions when user types
        ) }

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