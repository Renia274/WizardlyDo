package com.wizardlydo.app.screens.login.comps

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun LoginHeader() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = screenWidth > screenHeight

    Text(
        text = "Welcome Back",
        style = if (isLandscape) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = if (isLandscape) 16.dp else 32.dp)
    )
}

