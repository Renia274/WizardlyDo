package com.wizardlydo.app.screens.signup.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
    isCheckingAvailability: Boolean = false,
    suggestions: List<String> = emptyList()
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

        // Show suggestions when username is taken
        if (error?.contains("already taken") == true && suggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Try these instead:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { onNameChange(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }
    }
}