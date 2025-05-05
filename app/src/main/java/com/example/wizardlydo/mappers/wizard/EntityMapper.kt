package com.example.wizardlydo.mappers.wizard

import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.room.wizard.WizardEntity

fun WizardProfile.toEntity() = WizardEntity(
    userId = userId,
    wizardClass = wizardClass,
    wizardName = wizardName,
    email = email,
    passwordHash = passwordHash,
    signInProvider = signInProvider,
    level = level,
    experience = experience,
    health = health,
    maxHealth = maxHealth,
    gender = gender,
    skinColor = skinColor,
    hairStyle = hairStyle.toInt(),
    hairColor = hairColor,
    outfit = outfit,
    achievements = achievements,
    joinDate = joinDate,
    lastLogin = lastLogin,
    lastTaskCompleted = lastTaskCompleted,
    consecutiveTasksCompleted = consecutiveTasksCompleted,
    totalTasksCompleted = totalTasksCompleted,
    stamina = stamina,
    maxStamina = maxStamina,

)