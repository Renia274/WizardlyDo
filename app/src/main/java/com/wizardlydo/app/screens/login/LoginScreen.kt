package com.wizardlydo.app.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wizardlydo.app.comps.form.EmailField
import com.wizardlydo.app.comps.form.login.PasswordFieldLogin
import com.wizardlydo.app.screens.login.comps.LoginButton
import com.wizardlydo.app.screens.login.comps.LoginErrorDialog
import com.wizardlydo.app.screens.login.comps.LoginHeader
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.login.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            onBackClick = onBackClick,
            rememberMe = state.rememberMe,
            onRememberMeChange = viewModel::setRememberMe
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
    onBackClick: () -> Unit,
    rememberMe: Boolean = false,
    onRememberMeChange: (Boolean) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight

    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(8.dp, 24.dp)
    val maxWidth = if (screenWidth > 600.dp) 450.dp else screenWidth

    val backButtonTopPadding = if (isLandscape) 8.dp else 32.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = backButtonTopPadding, start = 2.dp),
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
                .padding(horizontal = padding)
                .padding(top = if (isLandscape) 60.dp else 80.dp),
            contentAlignment = if (isLandscape) Alignment.TopCenter else Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxWidth)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLandscape) {
                    Spacer(modifier = Modifier.height(spacing))
                }

                LoginHeader()

                Spacer(modifier = Modifier.height(if (isLandscape) spacing else spacing * 2))

                EmailField(
                    email = email,
                    onEmailChange = onEmailChange,
                    enabled = !isLoading,
                    emailError = emailError
                )

                Spacer(modifier = Modifier.height(if (isLandscape) spacing * 0.5f else spacing * 0.7f))

                PasswordFieldLogin(
                    password = password,
                    onPasswordChange = onPasswordChange,
                    isError = hasError,
                    errorMessage = passwordError,
                    enabled = !isLoading,
                    isPasswordVisible = isPasswordVisible,
                    onTogglePasswordVisibility = onTogglePasswordVisibility
                )

                Spacer(modifier = Modifier.height(spacing * 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remember Me Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = onRememberMeChange,
                            enabled = !isLoading,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Remember me",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Forgot Password
                    TextButton(
                        onClick = onForgotPasswordClick,
                        enabled = !isLoading
                    ) {
                        Text(
                            "Forgot Password?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) spacing * 0.8f else spacing * 1.5f))

                LoginButton(
                    onClick = onLoginClick,
                    isLoading = isLoading,
                    enabled = !isLoading && isFormValid
                )

                if (isLandscape) {
                    Spacer(modifier = Modifier.height(spacing))
                }
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
            onBackClick = {},
            rememberMe = true,
            onRememberMeChange = {}
        )
    }
}
