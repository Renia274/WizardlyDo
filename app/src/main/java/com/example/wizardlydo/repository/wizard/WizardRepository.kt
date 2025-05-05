package com.example.wizardlydo.repository.wizard

import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.mappers.toEntity
import com.example.wizardlydo.room.WizardDao
import com.example.wizardlydo.room.WizardEntity
import com.google.firebase.Timestamp
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
                signInProvider = profile.signInProvider, // Already SignInProvider enum
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
                spells = emptyList(),
                achievements = profile.achievements,
                joinDate = profile.joinDate,
                lastLogin = profile.lastLogin,
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
                signInProvider = entity.signInProvider, // Already SignInProvider enum
                level = entity.level,
                experience = entity.experience,
                health = entity.health,
                maxHealth = entity.maxHealth,
                stamina = entity.stamina,
                maxStamina = entity.maxStamina,
                gender = entity.gender,
                skinColor = entity.skinColor,
                hairColor = entity.hairColor,
                hairStyle = entity.hairStyle.toString(), // Convert int to string
                outfit = entity.outfit,
                lastTaskCompleted = entity.lastTaskCompleted,
                consecutiveTasksCompleted = entity.consecutiveTasksCompleted,
                totalTasksCompleted = entity.totalTasksCompleted,
                achievements = entity.achievements,
                joinDate = entity.joinDate,
                lastLogin =entity.lastLogin,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isSelected = false
            )
        }
    }

    suspend fun updateWizardLastLogin(userId: String): Result<Unit> = runCatching {
        val currentTime = Timestamp.now()
        wizardDao.updateLastLogin(userId, currentTime)
    }

    suspend fun updateWizardExperience(userId: String, experience: Int): Result<Unit> = runCatching {
        wizardDao.updateExperience(userId, experience)
    }

    suspend fun updateWizardLevel(userId: String, level: Int): Result<Unit> = runCatching {
        val currentTime = Timestamp.now()
        wizardDao.updateLevel(userId, level, currentTime)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
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
                achievements = entity.achievements,
                joinDate = entity.joinDate,
                lastLogin = entity.lastLogin,
                passwordHash = entity.passwordHash,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun updateWizardProfile(userId: String, profile: WizardProfile): Result<Unit> = runCatching {
        val entity = profile.toEntity()

        wizardDao.updateWizard(
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
            lastTaskCompleted = entity.lastTaskCompleted,
            consecutiveTasksCompleted = entity.consecutiveTasksCompleted,
            totalTasksCompleted = entity.totalTasksCompleted,
            spells = entity.spells,
            achievements = entity.achievements,
            joinDate = entity.joinDate,
            lastLogin = entity.lastLogin,
            gender = entity.gender,
            skinColor = entity.skinColor,
            hairStyle = entity.hairStyle,
            hairColor = entity.hairColor,
            outfit = entity.outfit,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    suspend fun updateWizardCustomization(
        userId: String,
        gender: String,
        skinColor: String,
        hairStyle: Int,
        hairColor: String,
        outfit: String,
        accessory: String = ""
    ): Result<Unit> = runCatching {
        wizardDao.updateWizardCustomization(
            userId = userId,
            gender = gender,
            skinColor = skinColor,
            hairStyle = hairStyle,
            hairColor = hairColor,
            outfit = outfit
        )
    }

    suspend fun isWizardNameTaken(wizardName: String): Result<Boolean> = runCatching {
        wizardDao.isWizardNameExists(wizardName)
    }
}