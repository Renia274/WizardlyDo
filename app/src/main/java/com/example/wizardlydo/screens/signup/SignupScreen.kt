package com.example.wizardlydo.screens.signup

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.screens.signup.comps.EmailField
import com.example.wizardlydo.screens.signup.comps.ErrorDialogComponent
import com.example.wizardlydo.screens.signup.comps.GoogleSignInButton
import com.example.wizardlydo.screens.signup.comps.LoginRedirectButton
import com.example.wizardlydo.screens.signup.comps.PasswordField
import com.example.wizardlydo.screens.signup.comps.SignupButton
import com.example.wizardlydo.screens.signup.comps.SignupHeader
import com.example.wizardlydo.viewModel.signup.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    onLoginClick: () -> Unit
) {
    val context = LocalActivity.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignupHeader()

        Spacer(modifier = Modifier.height(32.dp))

        EmailField(
            email = state.email,
            onEmailChange = viewModel::onEmailChange,
            emailError = state.emailError,
            enabled = !state.loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            password = state.password,
            onPasswordChange = viewModel::onPasswordChange,
            passwordError = state.passwordError,
            enabled = !state.loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignupButton(
            onClick = { viewModel.signupWithEmail() },
            isLoading = state.loading,
            enabled = !state.loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        GoogleSignInButton(
            onClick = { viewModel.signInWithGoogle(context) },
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