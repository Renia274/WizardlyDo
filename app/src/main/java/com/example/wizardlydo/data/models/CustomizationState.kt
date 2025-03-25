package com.example.wizardlydo.data.models

import com.example.wizardlydo.data.WizardClass

data class CustomizationState(
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val gender: String = "Male",
    val bodyColor: String = "#FFD700",
    val clothingColor: String = "#2E0854",
    val accessoryColor: String = "#000000",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)