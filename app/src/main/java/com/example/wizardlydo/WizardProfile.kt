package com.example.wizardlydo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

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