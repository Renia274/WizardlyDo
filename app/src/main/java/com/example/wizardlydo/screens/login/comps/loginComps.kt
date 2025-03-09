package com.example.wizardlydo.screens.login.comps

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginHeader() {
    Text(
        text = "Welcome Back",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 32.dp)
    )
}

@Composable
fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        isError = isError,
        enabled = enabled
    )
}

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = PasswordVisualTransformation(),
        isError = isError,
        enabled = enabled
    )
}

@Composable
fun LoginButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Login")
        }
    }
}

@Composable
fun ForgotPasswordButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text("Forgot Password?")
    }
}

@Composable
fun LoginErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login Error") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}