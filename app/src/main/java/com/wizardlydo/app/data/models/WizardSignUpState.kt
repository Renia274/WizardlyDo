package com.wizardlydo.app.data.models

import com.wizardlydo.app.data.wizard.WizardClass

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
    val isPasswordVisible: Boolean = false,
    val isCheckingUsername: Boolean
)
