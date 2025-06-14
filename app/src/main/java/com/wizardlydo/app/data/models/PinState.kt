package com.wizardlydo.app.data.models

data class PinState(
    val pin: String = "",
    val isPinSaved: Boolean = false,
    val isPinVerified: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
