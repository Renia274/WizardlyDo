package com.example.wizardlydo.screens.recovery

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.recovery.comps.BackToLoginButton
import com.example.wizardlydo.screens.recovery.comps.RecoveryEmailField
import com.example.wizardlydo.screens.recovery.comps.RecoveryErrorDialog
import com.example.wizardlydo.screens.recovery.comps.RecoveryHeader
import com.example.wizardlydo.screens.recovery.comps.RecoverySubmitButton
import com.example.wizardlydo.screens.recovery.comps.RecoverySuccessDialog
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.recovery.RecoveryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecoveryContent(
                email = state.email,
                onEmailChange = viewModel::updateEmail,
                onSubmitClick = viewModel::sendPasswordResetEmail,
                onBackToLoginClick = {
                    viewModel.resetState()
                    onNavigateToLogin()
                },
                emailError = state.emailError,
                isEmailValid = viewModel.isEmailValid,
                isLoading = state.isLoading
            )
        }

        state.error?.let { error ->
            RecoveryErrorDialog(
                error = error,
                onDismiss = viewModel::clearError
            )
        }

        if (state.isRecoveryEmailSent) {
            RecoverySuccessDialog(
                email = state.email,
                onDismiss = {
                    viewModel.resetState()
                    onNavigateToLogin()
                }
            )
        }
    }
}

@Composable
fun RecoveryContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    emailError: String?,
    isEmailValid: Boolean,
    isLoading: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Responsive values
    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.04f).coerceIn(16.dp, 32.dp)
    val maxWidth = if (screenWidth > 600.dp) 450.dp else screenWidth

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .widthIn(max = maxWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacer
        Spacer(modifier = Modifier.height((screenHeight * 0.1f).coerceIn(40.dp, 100.dp)))

        RecoveryHeader()

        Spacer(modifier = Modifier.height(spacing))

        RecoveryEmailField(
            email = email,
            onEmailChange = onEmailChange,
            emailError = emailError,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(spacing * 0.75f))

        RecoverySubmitButton(
            onClick = onSubmitClick,
            isLoading = isLoading,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(spacing * 0.5f))

        BackToLoginButton(
            onClick = onBackToLoginClick,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height((screenHeight * 0.1f).coerceIn(40.dp, 100.dp)))
    }
}

@Composable
@Preview(showBackground = true)
fun RecoveryContentPreview() {
    WizardlyDoTheme {
        RecoveryContent(
            email = "wizard@hogwarts.com",
            onEmailChange = {},
            onSubmitClick = {},
            onBackToLoginClick = {},
            emailError = null,
            isEmailValid = true,
            isLoading = false
        )
    }
}
