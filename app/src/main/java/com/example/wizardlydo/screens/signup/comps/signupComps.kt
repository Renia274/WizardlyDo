package com.example.wizardlydo.screens.signup.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.WizardClass


@Composable
fun SignupHeader() {
    Text(
        text = "Create Account",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        isError = emailError != null,
        supportingText = { emailError?.let { ErrorText(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    enabled: Boolean,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    painter = painterResource(id = if (isPasswordVisible) R.drawable.ic_show else R.drawable.ic_hide),
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                )
            }
        },
        isError = passwordError != null,
        supportingText = { passwordError?.let { ErrorText(it) } },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun SignupButton(
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
            Text("Sign Up")
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Continue with Google")
    }
}

@Composable
fun LoginRedirectButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Already have an account? Login")
    }
}

@Composable
fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ErrorDialogComponent(
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
fun WizardNameField(
    name: String,
    onNameChange: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Wizard Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = error != null,
            supportingText = { error?.let { ErrorText(it) } }
        )
    }
}

@Composable
fun WizardClassSelector(
    selectedClass: WizardClass,
    onClassSelected: (WizardClass) -> Unit,
    enabled: Boolean
) {
    Column {
        Text("Choose your class:", style = MaterialTheme.typography.bodyMedium)
        WizardClass.entries.forEach { wizardClass ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onClassSelected(wizardClass) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(
                    selected = wizardClass == selectedClass,
                    onClick = { onClassSelected(wizardClass) },
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(wizardClass.title, style = MaterialTheme.typography.bodyLarge)
                    Text(wizardClass.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}