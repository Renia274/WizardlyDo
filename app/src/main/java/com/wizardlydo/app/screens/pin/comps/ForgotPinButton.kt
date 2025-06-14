package com.wizardlydo.app.screens.pin.comps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ForgotPinButton(
    onForgotPin: () -> Unit
) {
    TextButton(
        onClick = onForgotPin,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Forgot PIN?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}