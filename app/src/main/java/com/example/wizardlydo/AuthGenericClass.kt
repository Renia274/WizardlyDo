package com.example.wizardlydo

sealed class WizardAuthResult {
    data class Success(val profile: WizardProfile) : WizardAuthResult()
    data class Error(val message: String) : WizardAuthResult()
    data object Loading : WizardAuthResult()
}