package com.example.wizardlydo.viewModel.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ewizardlydo.R
import com.example.wizardlydo.viewModel.signup.data.SignupState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val mutableState = MutableStateFlow(SignupState())
    val state = mutableState.asStateFlow()

    private val oneTapClient = Identity.getSignInClient(context)
    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .build()

    fun onEmailChange(email: String) {
        mutableState.update { it.copy(
            email = email,
            emailError = if (email.isBlank()) "Email cannot be empty" else null
        )}
    }

    fun onPasswordChange(password: String) {
        mutableState.update { it.copy(
            password = password,
            passwordError = if (password.length < 8) "Password must be at least 8 characters" else null
        )}
    }

    fun signupWithEmail(navController: NavController) {
        val currentState = mutableState.value

        if (currentState.email.isBlank() || currentState.password.length < 8) return

        mutableState.update { it.copy(loading = true) }

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(
                    currentState.email,
                    currentState.password
                ).await()

                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true }
                }
            } catch (e: Exception) {
                mutableState.update { it.copy(
                    error = e.message ?: "Signup failed",
                    loading = false
                )}
            }
        }
    }

    fun signInWithGoogle(activity: Activity, navController: NavController) {
        viewModelScope.launch {
            try {
                mutableState.update { it.copy(loading = true) }
                val result = oneTapClient.beginSignIn(signInRequest).await()
                activity.startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    GOOGLE_SIGN_IN_REQUEST_CODE,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        error = "Could not start Google sign in",
                        loading = false
                    )
                }
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?, navController: NavController) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken

            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                viewModelScope.launch {
                    try {
                        auth.signInWithCredential(firebaseCredential).await()
                        navController.navigate("home") {
                            popUpTo("signup") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        mutableState.update {
                            it.copy(
                                error = e.message ?: "Google sign-in failed",
                                loading = false
                            )
                        }
                    }
                }
            } else {
                mutableState.update {
                    it.copy(
                        error = "No ID token!",
                        loading = false
                    )
                }
            }
        } catch (e: Exception) {
            mutableState.update {
                it.copy(
                    error = "Google sign-in failed",
                    loading = false
                )
            }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }
}

