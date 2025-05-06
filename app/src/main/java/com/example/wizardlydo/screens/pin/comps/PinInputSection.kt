package com.example.wizardlydo.screens.pin.comps

import androidx.compose.runtime.Composable


@Composable
fun PinInputSection(
    pin: String,
    onPinChange: (String) -> Unit
) {
    PinInputField(
        pin = pin,
        onPinChange = onPinChange
    )
}
