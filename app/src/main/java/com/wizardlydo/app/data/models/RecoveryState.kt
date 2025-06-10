package com.wizardlydo.app.data.models

data class RecoveryState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isRecoveryEmailSent: Boolean = false,
    val error: String? = null,
    val emailError: String? = null
)
