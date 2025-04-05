package com.example.wizardlydo.repository

import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.mappers.toEntity
import com.example.wizardlydo.room.WizardDao
import com.example.wizardlydo.room.WizardEntity
import com.example.wizardlydo.room.WizardTypeConverters
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class WizardRepository(
    private val wizardDao: WizardDao,
    private val firebaseAuth: FirebaseAuth
) {
    private val typeConverters = WizardTypeConverters()

    suspend fun createWizardProfile(profile: WizardProfile): Result<Unit> {
        return try {
            val wizardEntity = WizardEntity(
                userId = profile.userId,
                wizardClass = profile.wizardClass,
                wizardName = profile.wizardName,
                email = profile.email,
                signInProvider = profile.signInProvider,
                level = profile.level,
                experience = profile.experience,
                achievements = profile.achievements,
                joinDate = profile.joinDate,
                lastLogin = profile.lastLogin,
                passwordHash = ""
            )
            wizardDao.insertWizard(wizardEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWizardProfile(userId: String): Result<WizardProfile?> {
        return try {
            val wizardEntity = wizardDao.getWizardById(userId)
            val profile = wizardEntity?.let { entity ->
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
                    gender = entity.gender,
                    bodyColor = entity.bodyColor,
                    clothingColor = entity.clothingColor,
                    accessoryColor = entity.accessoryColor
                )
            }
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardLastLogin(userId: String): Result<Unit> {
        return try {
            wizardDao.updateLastLogin(userId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardExperience(userId: String, experience: Int): Result<Unit> {
        return try {
            wizardDao.updateExperience(userId, experience)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWizardLevel(userId: String, level: Int): Result<Unit> {
        return try {
            wizardDao.updateLevel(userId, level, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // New method for sending password reset email via Firebase Auth
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findUserByEmail(email: String): Result<WizardProfile?> {
        return try {
            try {
                // This will throw an exception if the user doesn't exist
                firebaseAuth.fetchSignInMethodsForEmail(email).await()
            } catch (e: Exception) {
                return Result.success(null)
            }

            // If user exists in Firebase Auth, try to find in local database
            val wizardEntity = wizardDao.getWizardByEmail(email)
            val profile = wizardEntity?.let { entity ->
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
                    lastLogin = entity.lastLogin
                )
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun updateWizard(profile: WizardProfile) {
        val entity = profile.toEntity()
        val dao = wizardDao
        dao.updateWizard(
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
            bodyColor = entity.bodyColor,
            clothingColor = entity.clothingColor,
            accessoryColor = entity.accessoryColor
        )
    }


}

