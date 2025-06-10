package com.wizardlydo.app.data.models

data class PinSetupState(
    val pin: String = "",
    val isPinSaved: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isPinVerified: Boolean = false
)
