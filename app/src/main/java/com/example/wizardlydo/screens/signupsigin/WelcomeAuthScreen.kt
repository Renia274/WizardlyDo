package com.example.wizardlydo.screens.signupsigin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.screens.signupsigin.comps.SignInButton
import com.example.wizardlydo.screens.signupsigin.comps.SignUpButton
import com.example.wizardlydo.screens.signupsigin.comps.WelcomeMessage
import com.example.wizardlydo.ui.theme.WizardlyDoTheme


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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(16.dp, 32.dp)
    val verticalMargin = (screenHeight * 0.1f).coerceIn(40.dp, 100.dp)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(verticalMargin))

            WelcomeMessage()

            Spacer(modifier = Modifier.height(spacing * 2))

            SignUpButton(onClick = onSignUpClick)

            Spacer(modifier = Modifier.height(spacing))

            SignInButton(onClick = onSignInClick)

            Spacer(modifier = Modifier.height(verticalMargin))
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SignupContentPreview() {
    WizardlyDoTheme {
        WelcomeAuthContent(
            onSignUpClick = {},
            onSignInClick = {}
        )
    }
}
