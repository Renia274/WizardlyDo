package com.example.wizardlydo.mappers

import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.room.WizardEntity

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