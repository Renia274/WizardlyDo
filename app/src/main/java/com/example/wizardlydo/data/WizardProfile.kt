package com.example.wizardlydo.data

import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.providers.SignInProvider
import com.google.firebase.Timestamp

data class WizardProfile(
    val userId: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val wizardName: String = "",
    val email: String = "",
    val signInProvider: SignInProvider = SignInProvider.EMAIL,
    val level: Int = 1,
    val experience: Int = 0,
    val spells: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val joinDate: Timestamp? = null,
    val lastLogin: Timestamp? = null
)