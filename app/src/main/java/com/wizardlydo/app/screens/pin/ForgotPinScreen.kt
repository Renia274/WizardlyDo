package com.wizardlydo.app.screens.pin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wizardlydo.app.comps.errors.ErrorDialog
import com.wizardlydo.app.screens.pin.comps.ForgotPinHeader
import com.wizardlydo.app.screens.pin.comps.PinInputSection
import com.wizardlydo.app.screens.pin.comps.PinProgressIndicator
import com.wizardlydo.app.screens.pin.comps.ResetPinButton
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.pin.PinViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ForgotPinScreen(
    viewModel: PinViewModel = koinViewModel(),
    onPinResetComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isPinSaved) {
        if (state.isPinSaved) {
            onPinResetComplete()
        }
    }

    ForgotPinContent(
        pin = state.pin,
        onPinChange = viewModel::updatePin,
        onResetPin = viewModel::resetPin,
        isLoading = state.isLoading,
        hasError = state.error != null,
        error = state.error,
        onDismissError = viewModel::clearError
    )
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ForgotPinContent(
    pin: String,
    onPinChange: (String) -> Unit,
    onResetPin: () -> Unit,
    isLoading: Boolean,
    hasError: Boolean,
    error: String?,
    onDismissError: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(16.dp, 32.dp)

    // Auto-reset PIN when it's complete
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(500)
            onResetPin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height((screenHeight * 0.05f).coerceIn(20.dp, 50.dp)))

        ForgotPinHeader()

        Spacer(modifier = Modifier.height(spacing))

        PinInputSection(
            pin = pin,
            onPinChange = onPinChange
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        when {
            pin.length == 4 && isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resetting PIN...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            pin.length == 4 -> {
                ResetPinButton(
                    pin = pin,
                    onResetPin = onResetPin,
                    isLoading = isLoading
                )
            }
            else -> {
                Button(
                    onClick = onResetPin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = false
                ) {
                    Text("Enter new 4-digit PIN")
                }
            }
        }

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        Text(
            text = if (pin.length < 4) {
                "Enter your new 4-digit PIN"
            } else {
                "New PIN ready to save"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (pin.length == 4) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        PinProgressIndicator(
            currentLength = pin.length,
            totalLength = 4
        )

        Spacer(modifier = Modifier.height((screenHeight * 0.05f).coerceIn(20.dp, 50.dp)))
    }

    error?.let {
        ErrorDialog(
            error = it,
            onDismiss = onDismissError
        )
    }
}
@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun ForgotPinScreenPreview() {
    WizardlyDoTheme {
        ForgotPinContent(
            pin = "1234",
            onPinChange = {},
            onResetPin = {},
            isLoading = false,
            hasError = false,
            error = null,
            onDismissError = {}
        )
    }
}