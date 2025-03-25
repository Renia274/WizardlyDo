package com.example.wizardlydo.screens.signupsigin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.screens.signupsigin.comps.SignInButton
import com.example.wizardlydo.screens.signupsigin.comps.SignUpButton
import com.example.wizardlydo.screens.signupsigin.comps.WelcomeMessage


@Composable
fun WelcomeAuthScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        WelcomeAuthContent(
            onSignUpClick = onSignUpClick,
            onSignInClick = onSignInClick
        )
    }
}


@Composable
fun WelcomeAuthContent(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WelcomeMessage()
        SignUpButton(onClick = onSignUpClick)
        Spacer(modifier = Modifier.height(16.dp))
        SignInButton(onClick = onSignInClick)
    }
}