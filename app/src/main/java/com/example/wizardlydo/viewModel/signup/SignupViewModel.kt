package com.example.wizardlydo.viewModel.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.R
import com.example.wizardlydo.viewModel.signup.data.SignupState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state = _state.asStateFlow()

    // Email/Password Section
    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email.trim())
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password.trim())
    }

    fun signUpWithEmail() {
        if (!validateEmailPassword()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                auth.createUserWithEmailAndPassword(
                    _state.value.email,
                    _state.value.password
                ).await()
                _state.value = _state.value.copy(
                    authSuccess = true,
                    loading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Signup failed",
                    loading = false
                )
            }
        }
    }

    // Google Sign-In Section
    fun handleGoogleSignIn(token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val credential = GoogleAuthProvider.getCredential(token, null)
                auth.signInWithCredential(credential).await()
                _state.value = _state.value.copy(
                    authSuccess = true,
                    loading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Google sign-in failed: ${e.message}",
                    loading = false
                )
            }
        }
    }

    // Common
    private fun validateEmailPassword(): Boolean {
        val emailValid = Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()
        val passwordValid = _state.value.password.length >= 8

        _state.value = _state.value.copy(
            emailError = if (!emailValid) "Invalid email" else null,
            passwordError = if (!passwordValid) "8+ characters required" else null
        )

        return emailValid && passwordValid
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }


}