package com.example.wizardlydo.screens.signup.comps

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


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