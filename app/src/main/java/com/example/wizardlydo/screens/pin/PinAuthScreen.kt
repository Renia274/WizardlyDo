package com.example.wizardlydo.screens.pin

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.pin.comps.ErrorDialog
import com.example.wizardlydo.screens.pin.comps.PinAuthHeader
import com.example.wizardlydo.screens.pin.comps.PinInputSection
import com.example.wizardlydo.screens.pin.comps.PinVerifyButton
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.pin.PinViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun PinAuthScreen(
    viewModel: PinViewModel = koinViewModel(),
    onPinSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isPinVerified) {
        if (state.isPinVerified) {
            onPinSuccess()
        }
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
            PinAuthContent(
                pin = state.pin,
                onPinChange = viewModel::updatePin,
                onVerifyPin = viewModel::verifyPin,
                isLoading = state.isLoading,
                hasError = state.error != null,
                error = state.error,
                onDismissError = viewModel::clearError
            )
        }
    }
}
@Composable
fun PinAuthContent(
    pin: String,
    onPinChange: (String) -> Unit,
    onVerifyPin: () -> Unit,
    isLoading: Boolean,
    hasError: Boolean,
    error: String?,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 450.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinAuthHeader()

        Spacer(modifier = Modifier.height(32.dp))

        PinInputSection(
            pin = pin,
            onPinChange = onPinChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        PinVerifyButton(
            pin = pin,
            onVerifyPin = onVerifyPin,
            isLoading = isLoading
        )
    }

    // Show error dialog if needed - moved outside content layout
    error?.let {
        ErrorDialog(
            error = it,
            onDismiss = onDismissError
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PinAuthContentPreview() {
    WizardlyDoTheme {
        PinAuthContent(
            pin = "1234",
            onPinChange = {},
            onVerifyPin = {},
            isLoading = false,
            hasError = false,
            error = null,
            onDismissError = {}
        )
    }
}
