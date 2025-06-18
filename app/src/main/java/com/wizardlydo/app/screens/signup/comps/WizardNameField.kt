package com.wizardlydo.app.screens.signup.comps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.comps.ErrorText

@Composable
fun WizardNameField(
    name: String,
    onNameChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    isCheckingAvailability: Boolean = false
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Wizard Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = error != null,
            supportingText = {
                when {
                    isCheckingAvailability -> Text(
                        "Checking availability...",
                        color = MaterialTheme.colorScheme.primary
                    )
                    error != null -> ErrorText(error)
                    name.length >= 3 && !isCheckingAvailability -> Text(
                        "✓ Available",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            trailingIcon = {
                if (isCheckingAvailability) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        )
    }
}