package com.example.wizardlydo.data.models

data class PinSetupState(
    val pin: String = "",
    val isPinSaved: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)