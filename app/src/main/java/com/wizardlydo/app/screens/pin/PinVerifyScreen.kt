package com.wizardlydo.app.screens.pin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wizardlydo.app.comps.errors.ErrorDialog
import com.wizardlydo.app.screens.pin.comps.ForgotPinButton
import com.wizardlydo.app.screens.pin.comps.PinInputSection
import com.wizardlydo.app.screens.pin.comps.PinProgressIndicator
import com.wizardlydo.app.screens.pin.comps.PinVerifyButton
import com.wizardlydo.app.screens.pin.comps.PinVerifyHeader
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.pin.PinViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinVerifyScreen(
    viewModel: PinViewModel = koinViewModel(),
    onPinSuccess: () -> Unit,
    onForgotPin: () -> Unit,
    onSignupClick: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isPinVerified) {
        if (state.isPinVerified) {
            onPinSuccess()
        }
    }

    // Reset state when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

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
            PinVerifyContent(
                pin = state.pin,
                onPinChange = viewModel::updatePin,
                onVerifyPin = viewModel::verifyPin,
                onForgotPin = onForgotPin,
                onSignupClick = onSignupClick,
                isLoading = state.isLoading,
                hasError = state.error != null,
                error = state.error,
                onDismissError = viewModel::clearError
            )
        }
    }
}

@Composable
fun PinVerifyContent(
    pin: String,
    onPinChange: (String) -> Unit,
    onVerifyPin: () -> Unit,
    onForgotPin: () -> Unit,
    onSignupClick: (() -> Unit)? = null,
    isLoading: Boolean,
    hasError: Boolean,
    error: String?,
    onDismissError: () -> Unit
) {
    // Auto-verify PIN when it's complete
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(300)
            onVerifyPin()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 450.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinVerifyHeader()

        Spacer(modifier = Modifier.height(32.dp))

        PinInputSection(
            pin = pin,
            onPinChange = onPinChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            pin.length == 4 && isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verifying PIN...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            pin.length == 4 -> {
                PinVerifyButton(
                    pin = pin,
                    onVerifyPin = onVerifyPin,
                    isLoading = isLoading
                )
            }
            else -> {
                Button(
                    onClick = onVerifyPin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = false
                ) {
                    Text("Enter your PIN")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ForgotPinButton(
            onForgotPin = onForgotPin
        )

        onSignupClick?.let { signupClick ->

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Stay signed in?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sign up to keep your data and stay logged in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = signupClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Up")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PinProgressIndicator(
            currentLength = pin.length,
            totalLength = 4
        )
    }

    error?.let {
        ErrorDialog(
            error = it,
            onDismiss = onDismissError
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PinVerifyContentPreview() {
    WizardlyDoTheme {
        PinVerifyContent(
            pin = "1234",
            onPinChange = {},
            onVerifyPin = {},
            isLoading = false,
            hasError = false,
            error = null,
            onDismissError = {},
            onForgotPin = {},
            onSignupClick = {}
        )
    }
}