package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.WizardClass

data class WizardSignUpState(
    val email: String = "",
    val password: String = "",
    val wizardName: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val isLoading: Boolean = false,
    val isProfileComplete: Boolean = false,
    val error: String? = null
)