package com.example.wizardlydo.repository.wizard

import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.room.wizard.WizardDao
import com.example.wizardlydo.room.wizard.WizardEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface WizardRepository {
    val wizardDao: WizardDao
    val firebaseAuth: FirebaseAuth

    suspend fun createWizardProfile(profile: WizardProfile): Result<Unit> = runCatching {
        wizardDao.insertWizard(
            WizardEntity(
                userId = profile.userId,
                wizardClass = profile.wizardClass,
                wizardName = profile.wizardName,
                email = profile.email,
                passwordHash = profile.passwordHash,
                signInProvider = profile.signInProvider,
                level = profile.level,
                experience = profile.experience,
                health = profile.health,
                maxHealth = profile.maxHealth,
                stamina = profile.stamina,
                maxStamina = profile.maxStamina,
                gender = profile.gender,
                skinColor = profile.skinColor,
                hairStyle = profile.hairStyle.toIntOrNull() ?: 0,
                hairColor = profile.hairColor,
                outfit = profile.outfit,
                lastTaskCompleted = profile.lastTaskCompleted,
                consecutiveTasksCompleted = profile.consecutiveTasksCompleted,
                totalTasksCompleted = profile.totalTasksCompleted,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt
            )
        )
    }

    suspend fun getWizardProfile(userId: String): Result<WizardProfile?> = runCatching {
        wizardDao.getWizardById(userId)?.let { entity ->
            WizardProfile(
                userId = entity.userId,
                wizardClass = entity.wizardClass,
                wizardName = entity.wizardName,
                email = entity.email,
                passwordHash = entity.passwordHash,
                signInProvider = entity.signInProvider,
                level = entity.level,
                experience = entity.experience,
                health = entity.health,
                maxHealth = entity.maxHealth,
                stamina = entity.stamina,
                maxStamina = entity.maxStamina,
                gender = entity.gender,
                skinColor = entity.skinColor,
                hairColor = entity.hairColor,
                hairStyle = entity.hairStyle.toString(),
                outfit = entity.outfit,
                lastTaskCompleted = entity.lastTaskCompleted,
                consecutiveTasksCompleted = entity.consecutiveTasksCompleted,
                totalTasksCompleted = entity.totalTasksCompleted,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isSelected = false
            )
        }
    }

    suspend fun findUserByEmail(email: String): Result<WizardProfile?> = runCatching {
        try {
            firebaseAuth.fetchSignInMethodsForEmail(email).await()
        } catch (e: Exception) {
            return@runCatching null
        }

        wizardDao.getWizardByEmail(email)?.let { entity ->
            WizardProfile(
                userId = entity.userId,
                wizardClass = entity.wizardClass,
                wizardName = entity.wizardName,
                email = entity.email,
                signInProvider = entity.signInProvider,
                level = entity.level,
                experience = entity.experience,
                health = entity.health,
                maxHealth = entity.maxHealth,
                stamina = entity.stamina,
                maxStamina = entity.maxStamina,
                passwordHash = entity.passwordHash,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun updateWizardProfile(userId: String, profile: WizardProfile): Result<Unit> = runCatching {
        wizardDao.updateWizard(
            WizardEntity(
                userId = profile.userId,
                wizardClass = profile.wizardClass,
                wizardName = profile.wizardName,
                email = profile.email,
                passwordHash = profile.passwordHash,
                signInProvider = profile.signInProvider,
                level = profile.level,
                experience = profile.experience,
                health = profile.health,
                maxHealth = profile.maxHealth,
                stamina = profile.stamina,
                maxStamina = profile.maxStamina,
                gender = profile.gender,
                skinColor = profile.skinColor,
                hairStyle = profile.hairStyle.toIntOrNull() ?: 0,
                hairColor = profile.hairColor,
                outfit = profile.outfit,
                lastTaskCompleted = profile.lastTaskCompleted,
                consecutiveTasksCompleted = profile.consecutiveTasksCompleted,
                totalTasksCompleted = profile.totalTasksCompleted,
                createdAt = profile.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }



    suspend fun updateWizardCustomization(
        userId: String,
        skinColor: String,
        hairStyle: Int,
        gender: String,
        hairColor: String,
        outfit: String
    ): Result<Unit> = runCatching {
        wizardDao.updateWizardCustomization(
            userId = userId,
            skinColor = skinColor,
            hairStyle = hairStyle,
            hairColor = hairColor,
            gender = gender

        )
    }



    suspend fun isWizardNameTaken(wizardName: String): Result<Boolean> = runCatching {
        wizardDao.isWizardNameExists(wizardName)
    }


}