package com.wizardlydo.app.screens.pin.comps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun SavePinButton(
    pin: String,
    onSavePin: () -> Unit
) {
    Button(
        onClick = onSavePin,
        enabled = pin.length == 4,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save PIN")
    }
}
