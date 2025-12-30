package com.wizardlydo.app.screens.pin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
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

    // Responsive sizing calculations
    val horizontalPadding = (screenWidth * 0.08f).coerceIn(24.dp, 48.dp)
    val maxContentWidth = (screenWidth * 0.85f).coerceIn(300.dp, 500.dp)
    val verticalSpacing = (screenHeight * 0.025f).coerceIn(16.dp, 32.dp)
    val headerSize = (screenWidth * 0.15f).coerceIn(60.dp, 100.dp)

    // Auto-reset PIN when it's complete
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(500)
            onResetPin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Add some top spacing for smaller screens
            Spacer(modifier = Modifier.height(verticalSpacing))

            ForgotPinHeader()

            Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

            PinInputSection(
                pin = pin,
                onPinChange = onPinChange
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 1.2f))

            when {
                pin.length == 4 && isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size((screenWidth * 0.1f).coerceIn(36.dp, 48.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                            .height((screenHeight * 0.065f).coerceIn(48.dp, 60.dp)),
                        enabled = false
                    ) {
                        Text(
                            text = "Enter new 4-digit PIN",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

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
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 0.75f))

            PinProgressIndicator(
                currentLength = pin.length,
                totalLength = 4
            )

            // Add some bottom spacing for smaller screens
            Spacer(modifier = Modifier.height(verticalSpacing))
        }
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