package com.example.wizardlydo.data

import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.room.WizardEntity
import com.google.firebase.Timestamp

data class WizardProfile(
    // Core Identification
    val userId: String = "",
    val wizardName: String = "",
    val email: String = "",
    val signInProvider: SignInProvider = SignInProvider.EMAIL,
    val passwordHash: String = "",

    // Appearance Customization
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val gender: String = "Male",
    val skinColor: String = "light",
    val hairColor: String = "brown",
    val hairStyle: Int = 0,
    val outfit: String = "",


    // Color Settings (Legacy Support)
    val headColor: String = "#FFD700",
    val bodyColor: String = "#FFD700",
    val legsColor: String = "#2E0854",
    val armsColor: String = "#2E0854",
    val clothingColor: String = "#8E44AD",

    // Progression System
    val level: Int = 1,
    val experience: Int = 0,
    val health: Int = 100,
    val maxHealth: Int = 100,
    val achievements: List<String> = emptyList(),

    // Task System Integration
    val lastTaskCompleted: Timestamp? = null,
    val consecutiveTasksCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,

    // Timestamps
    val joinDate: Timestamp? = null,
    val lastLogin: Timestamp? = null,


    val stamina: Int = 75,

)

private fun WizardProfile.toEntity() = WizardEntity(
    userId = userId,
    wizardClass = wizardClass,
    wizardName = wizardName,
    email = email,
    passwordHash = "",
    signInProvider = signInProvider,
    gender = gender,
    skinColor = skinColor,
    hairColor = hairColor,
    hairStyle = hairStyle,
    outfit = outfit,
    level = level,
    experience = experience,
    health = health,
    maxHealth = maxHealth,
    lastTaskCompleted = lastTaskCompleted,
    consecutiveTasksCompleted = consecutiveTasksCompleted,
    totalTasksCompleted = totalTasksCompleted,
    achievements = achievements,
    joinDate = joinDate,
    lastLogin = lastLogin
)