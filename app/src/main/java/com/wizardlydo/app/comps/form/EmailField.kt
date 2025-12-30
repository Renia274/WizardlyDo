package com.wizardlydo.app.comps.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.wizardlydo.app.comps.ErrorText

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
            imeAction = ImeAction.Next,
            autoCorrect = false
        ),
        isError = emailError != null,
        supportingText = { emailError?.let { ErrorText(it) } },
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentType = ContentType.EmailAddress
            },
        enabled = enabled,
        singleLine = true
    )
}