package com.example.wizardlydo.screens.pin

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.pin.comps.ErrorDialog
import com.example.wizardlydo.screens.pin.comps.PinAuthHeader
import com.example.wizardlydo.screens.pin.comps.PinInputSection
import com.example.wizardlydo.screens.pin.comps.PinVerifyButton
import com.example.wizardlydo.viewmodel.PinViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinAuthScreen(
    viewModel: PinViewModel = koinViewModel(),
    onPinSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isPinSaved) {
        if (state.isPinSaved) {
            onPinSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        // Error Dialog within Content
        error?.let {
            ErrorDialog(
                error = it,
                onDismiss = onDismissError
            )
        }
    }
}