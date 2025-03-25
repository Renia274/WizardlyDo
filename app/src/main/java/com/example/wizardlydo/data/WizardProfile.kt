package com.example.wizardlydo.data

import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.room.WizardEntity
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
    val lastLogin: Timestamp? = null,
    val gender: String = "Male",
    val bodyColor: String = "#FFD700",
    val clothingColor: String = "#2E0854",
    val accessoryColor: String = "#000000",
    val passwordHash: String = ""
)



private fun WizardProfile.toEntity() = WizardEntity(
    userId = userId,
    wizardClass = wizardClass,
    wizardName = wizardName,
    email = email,
    passwordHash = "",
    signInProvider = signInProvider,
    gender = gender,
    bodyColor = bodyColor,
    clothingColor = clothingColor,
    accessoryColor = accessoryColor,
    level = level,
    experience = experience,
    spells = spells,
    achievements = achievements,
    joinDate = joinDate,
    lastLogin = lastLogin
)