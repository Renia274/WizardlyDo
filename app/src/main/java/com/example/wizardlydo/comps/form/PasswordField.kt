package com.example.wizardlydo.comps.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.comps.ErrorText


@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    enabled: Boolean,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (passwordError == null && password.isNotEmpty()) {
            PasswordStrengthIndicator(password = password)
        }
    }
}