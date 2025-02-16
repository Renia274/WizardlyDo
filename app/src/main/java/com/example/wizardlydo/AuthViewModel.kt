package com.example.wizardlydo

import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    data class EmailSignUpSuccess(val user: FirebaseUser?) : AuthState()
    data class EmailSignUpError(val message: String) : AuthState()


}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null
)

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    // Email/Password Sign Up
    fun signUpWithEmail(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.EmailSignUpError("Invalid email format")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.EmailSignUpError("Password must be at least 6 characters")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.EmailSignUpSuccess(auth.currentUser)
                    _loginState.value = loginState.value.copy(
                        user = auth.currentUser,
                        isLoading = false
                    )
                } else {
                    _authState.value = AuthState.EmailSignUpError(
                        task.exception?.message ?: "Registration failed"
                    )
                    _loginState.value = loginState.value.copy(
                        isLoading = false,
                        error = task.exception?.message
                    )
                }
            }
    }

    // Google Sign-In
    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    fun handleGoogleSignInResult(data: Intent?) {
        _authState.value = AuthState.Loading
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(auth.currentUser)
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    // Input validation
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}