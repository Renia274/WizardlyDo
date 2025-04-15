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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.recovery.comps.BackToLoginButton
import com.example.wizardlydo.screens.recovery.comps.RecoveryEmailField
import com.example.wizardlydo.screens.recovery.comps.RecoveryErrorDialog
import com.example.wizardlydo.screens.recovery.comps.RecoveryHeader
import com.example.wizardlydo.screens.recovery.comps.RecoverySubmitButton
import com.example.wizardlydo.screens.recovery.comps.RecoverySuccessDialog
import com.example.wizardlydo.viewmodel.RecoveryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize(),
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
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 450.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecoveryHeader()

        Spacer(modifier = Modifier.height(32.dp))

        RecoveryEmailField(
            email = email,
            onEmailChange = onEmailChange,
            emailError = emailError,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        RecoverySubmitButton(
            onClick = onSubmitClick,
            isLoading = isLoading,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        BackToLoginButton(
            onClick = onBackToLoginClick,
            enabled = !isLoading
        )
    }
}