package com.example.wizardlydo.screens.signupsigin.comps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
 fun WelcomeMessage() {
    Text(
        text = "Welcome to Our App",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 48.dp)
    )
}

@Composable
 fun SignUpButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Create Account")
    }
}

@Composable
 fun SignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign In")
    }
}
