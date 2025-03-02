package com.example.wizardlydo

import com.example.wizardlydo.data.WizardProfile

sealed class WizardAuthResult {
    data class Success(val profile: WizardProfile) : WizardAuthResult()
    data class Error(val message: String) : WizardAuthResult()
    data object Loading : WizardAuthResult()
}