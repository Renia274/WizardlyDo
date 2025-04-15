package com.example.wizardlydo.screens.pin

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wizardlydo.screens.pin.comps.ErrorDialog
import com.example.wizardlydo.screens.pin.comps.PinInputSection
import com.example.wizardlydo.screens.pin.comps.PinSetupHeader
import com.example.wizardlydo.screens.pin.comps.SavePinButton
import com.example.wizardlydo.viewmodel.PinViewModel
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
                isLoading = false,
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

        SavePinButton(
            pin = pin,
            onSavePin = onSavePin
        )


    }

    error?.let {
        ErrorDialog(
            error = it,
            onDismiss = onDismissError
        )
    }
}