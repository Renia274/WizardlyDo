package com.wizardlydo.app.data.models

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false
)
