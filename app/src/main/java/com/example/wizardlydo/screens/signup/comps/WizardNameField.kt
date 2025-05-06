package com.example.wizardlydo.screens.signup.comps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wizardlydo.comps.ErrorText

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

