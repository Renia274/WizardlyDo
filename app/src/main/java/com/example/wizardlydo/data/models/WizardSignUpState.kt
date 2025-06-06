package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.wizard.WizardClass

data class WizardSignUpState(
    val email: String = "",
    val password: String = "",
    val wizardName: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val isLoading: Boolean = false,
    val isProfileComplete: Boolean = false,
    val error: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false
)