package com.example.wizardlydo.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.login.comps.EmailField
import com.example.wizardlydo.screens.login.comps.LoginButton
import com.example.wizardlydo.screens.login.comps.LoginErrorDialog
import com.example.wizardlydo.screens.login.comps.LoginHeader
import com.example.wizardlydo.screens.login.comps.PasswordField
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.login.LoginViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginContent(
            email = state.email,
            password = state.password,
            onEmailChange = viewModel::updateEmail,
            onPasswordChange = viewModel::updatePassword,
            onLoginClick = viewModel::login,
            onForgotPasswordClick = onForgotPasswordClick,
            isFormValid = viewModel.isFormValid,
            isLoading = state.isLoading,
            hasError = state.error != null,
            emailError = state.emailError,
            passwordError = state.passwordError,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onBackClick = onBackClick
        )


        state.error?.let { error ->
            LoginErrorDialog(
                error = error,
                onDismiss = viewModel::clearError
            )
        }
    }
}

@Composable
fun LoginContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    emailError: String? = null,
    passwordError: String? = null,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp


    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(8.dp, 24.dp)
    val maxWidth = if (screenWidth > 600.dp) 450.dp else screenWidth

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp),
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Signup",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxWidth)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginHeader()

                Spacer(modifier = Modifier.height(spacing * 2))

                EmailField(
                    email = email,
                    onEmailChange = onEmailChange,
                    isError = hasError,
                    errorMessage = emailError,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(spacing))

                PasswordField(
                    password = password,
                    onPasswordChange = onPasswordChange,
                    isError = hasError,
                    errorMessage = passwordError,
                    enabled = !isLoading,
                    isPasswordVisible = isPasswordVisible,
                    onTogglePasswordVisibility = onTogglePasswordVisibility
                )

                Spacer(modifier = Modifier.height(spacing * 0.5f))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        enabled = !isLoading
                    ) {
                        Text("Forgot Password?")
                    }
                }

                Spacer(modifier = Modifier.height(spacing * 1.5f))

                LoginButton(
                    onClick = onLoginClick,
                    isLoading = isLoading,
                    enabled = !isLoading
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    WizardlyDoTheme {
        LoginContent(
            email = "user@example.com",
            password = "password123",
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            isFormValid = true,
            isLoading = false,
            hasError = false,
            emailError = null,
            passwordError = null,
            isPasswordVisible = false,
            onTogglePasswordVisibility = {},
            onBackClick = {}
        )
    }
}