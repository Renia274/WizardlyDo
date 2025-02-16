package com.example.wizardlydo.screens.signup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wizardlydo.R
import com.example.wizardlydo.screens.signup.comps.EmailField
import com.example.wizardlydo.screens.signup.comps.ErrorDialogComponent
import com.example.wizardlydo.screens.signup.comps.GoogleSignInButton
import com.example.wizardlydo.screens.signup.comps.LoginRedirectButton
import com.example.wizardlydo.screens.signup.comps.PasswordField
import com.example.wizardlydo.screens.signup.comps.SignupButton
import com.example.wizardlydo.screens.signup.comps.SignupHeader
import com.example.wizardlydo.viewModel.signup.SignupViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.addOnSuccessListener { account ->
                account.idToken?.let { token ->
                    viewModel.handleGoogleSignIn(token)
                }
            }.addOnFailureListener {
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(state.authSuccess) {
        if (state.authSuccess) onSignupSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignupHeader()
            Spacer(modifier = Modifier.height(32.dp))

            EmailField(
                email = state.email,
                onEmailChange = { viewModel.updateEmail(it) },
                emailError = state.emailError,
                enabled = !state.loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                password = state.password,
                onPasswordChange = { viewModel.updatePassword(it) },
                passwordError = state.passwordError,
                enabled = !state.loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            SignupButton(
                onClick = { viewModel.signUpWithEmail() },
                isLoading = state.loading,
                enabled = !state.loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                enabled = !state.loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginRedirectButton(
                onClick = onLoginClick,
                enabled = !state.loading
            )
        }

        state.error?.let { error ->
            ErrorDialogComponent(
                error = error,
                onDismiss = viewModel::clearError
            )
        }
    }
}