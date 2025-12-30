package com.wizardlydo.app.models

import com.wizardlydo.app.data.wizard.WizardClass

data class CustomizationState(
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val gender: String = "Male",
    val skinColor: String = "light",
    val hairStyle: Int = 0,
    val hairColor: String = "brown",
    val outfit: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
