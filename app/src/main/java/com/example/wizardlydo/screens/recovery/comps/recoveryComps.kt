package com.example.wizardlydo.screens.recovery.comps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RecoveryHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your email to receive password reset instructions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun RecoveryEmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        isError = emailError != null,
        enabled = enabled,
        supportingText = emailError?.let { { Text(it) } }
    )
}

@Composable
fun RecoverySubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Send Recovery Email")
        }
    }
}

@Composable
fun BackToLoginButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Back to Login")
    }
}

@Composable
fun RecoveryErrorDialog(
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
fun RecoverySuccessDialog(
    email: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Email Sent") },
        text = {
            Text("A password reset link has been sent to $email. Please check your inbox and follow the instructions.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}