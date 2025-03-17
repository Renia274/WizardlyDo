package com.example.wizardlydo.screens.pin.comps

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun PinSetupHeader() {
    Text(
        text = "Set Up Security PIN",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PinAuthHeader() {
    Text(
        text = "Enter PIN",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PinInputSection(
    pin: String,
    onPinChange: (String) -> Unit
) {
    PinInputField(
        pin = pin,
        onPinChange = onPinChange
    )
}



@Composable
fun SavePinButton(
    pin: String,
    onSavePin: () -> Unit
) {
    Button(
        onClick = onSavePin,
        enabled = pin.length == 4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save PIN")
    }
}

@Composable
fun PinInputField(
    pin: String,
    onPinChange: (String) -> Unit
) {
    BasicTextField(
        value = pin,
        onValueChange = {
            // Limit to 4 digits
            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                onPinChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        decorationBox = { innerTextField ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    PinDigitBox(
                        digit = pin.getOrNull(index)?.toString() ?: "",
                        isFocused = pin.length == index
                    )
                }
            }
        }
    )
}

@Composable
fun PinDigitBox(
    digit: String,
    isFocused: Boolean
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (digit.isNotEmpty()) "•" else "",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun PinVerifyButton(
    pin: String,
    onVerifyPin: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onVerifyPin,
        enabled = pin.length == 4 && !isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Verify PIN")
        }
    }
}