package com.wizardlydo.app.screens.pin.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun PinInputField(
    pin: String,
    onPinChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Hide keyboard when PIN is complete
    LaunchedEffect(pin.length) {
        if (pin.length == 4) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    BasicTextField(
        value = pin,
        onValueChange = { newPin ->
            if (newPin.length <= 4 && newPin.all { char -> char.isDigit() }) {
                onPinChange(newPin)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            // Hide the actual text field
            Box(modifier = Modifier.size(0.dp)) {
                innerTextField()
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    PinDigitBox(
                        digit = pin.getOrNull(index)?.toString() ?: "",
                        isFocused = pin.length == index && pin.length < 4,
                        showCursor = pin.length == index && pin.length < 4
                    )
                }
            }
        }
    )
}