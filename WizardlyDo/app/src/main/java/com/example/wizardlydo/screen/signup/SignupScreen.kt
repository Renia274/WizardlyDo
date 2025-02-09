package com.example.wizardlydo.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ewizardlydo.R
import com.example.wizardlydo.viewModel.signup.SignupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = state.emailError != null,
            supportingText = { state.emailError?.let { ErrorText(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = if (state.passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                PasswordVisibilityIcon(
                    visible = state.passwordVisible,
                    onToggle = { viewModel.togglePasswordVisibility() }
                )
            },
            isError = state.passwordError != null,
            supportingText = { state.passwordError?.let { ErrorText(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Signup Button
        Button(
            onClick = { viewModel.signupWithEmail(navController) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            if (state.loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In
        OutlinedButton(
            onClick = { viewModel.signInWithGoogle(context, navController) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continue with Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Redirect
        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }

    // Handle errors
    if (state.error != null) {
        ErrorDialog(
            message = state.error,
            onDismiss = { viewModel.clearError() }
        )
    }
}

@Composable
private fun PasswordVisibilityIcon(visible: Boolean, onToggle: () -> Unit) {
    val image = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
    IconButton(onClick = onToggle) {
        Icon(imageVector = image, contentDescription = "Toggle password visibility")
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}