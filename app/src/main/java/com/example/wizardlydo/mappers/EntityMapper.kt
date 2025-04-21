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
    achievements = achievements,
    joinDate = joinDate,
    lastLogin = lastLogin,
    gender = gender,

)