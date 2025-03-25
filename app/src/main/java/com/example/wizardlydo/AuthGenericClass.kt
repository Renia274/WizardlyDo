package com.example.wizardlydo

import com.example.wizardlydo.data.WizardProfile

sealed class WizardResult {
    data class Success(val profile: WizardProfile) : WizardResult()
    data class Error(val message: String) : WizardResult()
    data object Loading : WizardResult()
}