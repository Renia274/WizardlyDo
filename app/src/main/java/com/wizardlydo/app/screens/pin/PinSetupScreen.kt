package com.wizardlydo.app.screens.pin

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wizardlydo.app.comps.errors.ErrorDialog
import com.wizardlydo.app.screens.pin.comps.PinInputSection
import com.wizardlydo.app.screens.pin.comps.PinProgressIndicator
import com.wizardlydo.app.screens.pin.comps.PinSetupHeader
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.pin.PinViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PinSetupScreen(
    viewModel: PinViewModel = koinViewModel(),
    onPinSetupComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isPinSaved) {
        if (state.isPinSaved) {
            onPinSetupComplete()
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
            PinSetupContent(
                pin = state.pin,
                onPinChange = viewModel::updatePin,
                onSavePin = viewModel::validateAndSavePin,
                isLoading = state.isLoading,
                hasError = state.error != null,
                error = state.error,
                onDismissError = viewModel::clearError
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PinSetupContent(
    pin: String,
    onPinChange: (String) -> Unit,
    onSavePin: () -> Unit,
    isLoading: Boolean,
    hasError: Boolean,
    error: String?,
    onDismissError: () -> Unit,
) {
    // Auto-save PIN when it's complete
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(500)
            onSavePin()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 450.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinSetupHeader()

        Spacer(modifier = Modifier.height(32.dp))

        PinInputSection(
            pin = pin,
            onPinChange = onPinChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show different states based on PIN completion
        when {
            pin.length == 4 && isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saving PIN...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            pin.length == 4 && !isLoading -> {
                Button(
                    onClick = onSavePin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = true
                ) {
                    Text("Save PIN")
                }
            }
            else -> {
                Button(
                    onClick = onSavePin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = false
                ) {
                    Text("Enter 4-digit PIN")
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

@RequiresApi(Build.VERSION_CODES.R)
@Composable
@Preview(showBackground = true)
fun PinSetupContentPreview() {
    WizardlyDoTheme {
        PinSetupContent(
            pin = "1234",
            onPinChange = {},
            onSavePin = {},
            isLoading = false,
            hasError = false,
            error = null,
            onDismissError = {}
        )
    }
}