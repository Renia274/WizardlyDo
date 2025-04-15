package com.example.wizardlydo.data

import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.room.WizardEntity
import com.google.firebase.Timestamp

data class WizardProfile(
    val userId: String = "",
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val gender: String = "Male",
    val skinColor: String = "light",
    // Original color fields (keeping for backward compatibility)
    val headColor: String = "#FFD700",
    val bodyColor: String = "#FFD700",
    val legsColor: String = "#2E0854",
    val armsColor: String = "#2E0854",
    val weaponColor: String = "#8E44AD",
    val accessoryColor: String = "#FFD700",
    val hairColor: String = "brown",
    val clothingColor: String = "#8E44AD",
    // Hair style as integer index
    val hairStyle: Int = 0,
    val outfit: String = "",
    val accessory: String = "",
    // Character information
    val wizardName: String = "",
    val email: String = "",
    val signInProvider: SignInProvider = SignInProvider.EMAIL,
    // Character progression
    val level: Int = 1,
    val experience: Int = 0,
    // Task management related fields (new fields)
    val health: Int = 100,
    val maxHealth: Int = 100,
    val lastTaskCompleted: Timestamp? = null,
    val consecutiveTasksCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val achievements: List<String> = emptyList(),
    // Timestamp fields
    val joinDate: Timestamp? = null,
    val lastLogin: Timestamp? = null,
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
    skinColor = skinColor,
    bodyColor = bodyColor,
    clothingColor = clothingColor,
    hairColor = hairColor,
    hairStyle = hairStyle,
    outfit = outfit,
    accessory = accessory,
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