package com.example.wizardlydo.repository.wizard

import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.mappers.toEntity
import com.example.wizardlydo.room.WizardDao
import com.example.wizardlydo.room.WizardEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlin.Result
import kotlin.runCatching

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
                signInProvider = profile.signInProvider,
                level = profile.level,
                experience = profile.experience,
                health = profile.health,
                maxHealth = profile.maxHealth,
                gender = profile.gender,
                skinColor = profile.skinColor,
                hairStyle = profile.hairStyle,
                hairColor = profile.hairColor,
                outfit = profile.outfit,
                lastTaskCompleted = profile.lastTaskCompleted,
                consecutiveTasksCompleted = profile.consecutiveTasksCompleted,
                totalTasksCompleted = profile.totalTasksCompleted,
                achievements = profile.achievements,
                joinDate = profile.joinDate,
                lastLogin = profile.lastLogin,
                passwordHash = profile.passwordHash,
                stamina = profile.stamina
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
                signInProvider = entity.signInProvider,
                level = entity.level,
                experience = entity.experience,
                health = entity.health,
                maxHealth = entity.maxHealth,
                gender = entity.gender,
                skinColor = entity.skinColor,
                hairColor = entity.hairColor,
                hairStyle = entity.hairStyle,
                outfit = entity.outfit,
                lastTaskCompleted = entity.lastTaskCompleted,
                consecutiveTasksCompleted = entity.consecutiveTasksCompleted,
                totalTasksCompleted = entity.totalTasksCompleted,
                achievements = entity.achievements,
                joinDate = entity.joinDate,
                lastLogin = entity.lastLogin,
                passwordHash = entity.passwordHash,
                stamina = entity.stamina
            )
        }
    }

    suspend fun updateWizardLastLogin(userId: String): Result<Unit> = runCatching {
        val currentTime = Timestamp.now().toDate().time
        wizardDao.updateLastLogin(userId, currentTime)
    }

    suspend fun updateWizardExperience(userId: String, experience: Int): Result<Unit> = runCatching {
        wizardDao.updateExperience(userId, experience)
    }

    suspend fun updateWizardLevel(userId: String, level: Int): Result<Unit> = runCatching {
        val currentTime = Timestamp.now().toDate().time
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
                achievements = entity.achievements,
                joinDate = entity.joinDate,
                lastLogin = entity.lastLogin,
            )
        }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun updateWizardProfile(userId: String, profile: WizardProfile): Result<Unit> = runCatching {
        val entity = profile.toEntity().copy(
            joinDate = profile.joinDate,
            lastLogin = profile.lastLogin,
            health = profile.health,
            stamina = profile.stamina,
            experience = profile.experience,
            level = profile.level
        )

        wizardDao.updateWizard(
            userId = entity.userId,
            wizardClass = entity.wizardClass,
            wizardName = entity.wizardName,
            email = entity.email,
            passwordHash = entity.passwordHash,
            signInProvider = entity.signInProvider,
            level = entity.level,
            experience = entity.experience,
            spells = entity.spells,
            achievements = entity.achievements,
            joinDate = entity.joinDate,
            lastLogin = entity.lastLogin,
            gender = entity.gender,
            skinColor = entity.skinColor,
            hairStyle = entity.hairStyle,
            hairColor = entity.hairColor,
            outfit = entity.outfit
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
            userId = userId.toInt(),
            gender = gender,
            skinColor = skinColor,
            hairStyle = hairStyle,
            hairColor = hairColor,
            outfit = outfit,
            accessory = accessory
        )
    }

    suspend fun isWizardNameTaken(wizardName: String): Result<Boolean> = runCatching {
        wizardDao.isWizardNameExists(wizardName)
    }
}