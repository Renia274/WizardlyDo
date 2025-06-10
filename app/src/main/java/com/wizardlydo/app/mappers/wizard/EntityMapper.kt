package com.wizardlydo.app.mappers.wizard

import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.room.wizard.WizardEntity

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
    lastTaskCompleted = lastTaskCompleted,
    consecutiveTasksCompleted = consecutiveTasksCompleted,
    totalTasksCompleted = totalTasksCompleted,
    stamina = stamina,
    maxStamina = maxStamina,

)
