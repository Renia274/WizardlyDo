package com.example.wizardlydo.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.providers.SignInProvider
import com.google.firebase.Timestamp

@Dao
interface WizardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWizard(wizard: WizardEntity)

    @Query("SELECT * FROM wizards WHERE userId = :userId")
    suspend fun getWizardById(userId: String): WizardEntity?

    @Query("SELECT * FROM wizards WHERE email = :email")
    suspend fun getWizardByEmail(email: String): WizardEntity?

    @Query(
        """
        UPDATE wizards 
        SET 
            wizardClass = :wizardClass,
            wizardName = :wizardName,
            email = :email,
            passwordHash = :passwordHash,
            signInProvider = :signInProvider,
            level = :level,
            experience = :experience,
            health = :health,
            maxHealth = :maxHealth,
            stamina = :stamina,
            maxStamina = :maxStamina,
            spells = :spells,
            achievements = :achievements,
            joinDate = :joinDate,
            lastLogin = :lastLogin,
            lastTaskCompleted = :lastTaskCompleted,
            consecutiveTasksCompleted = :consecutiveTasksCompleted,
            totalTasksCompleted = :totalTasksCompleted,
            gender = :gender,
            skinColor = :skinColor,
            hairStyle = :hairStyle,
            hairColor = :hairColor,
            outfit = :outfit,
            createdAt = :createdAt,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """
    )
    suspend fun updateWizard(
        userId: String,
        wizardClass: WizardClass,
        wizardName: String,
        email: String,
        passwordHash: String,
        signInProvider: SignInProvider,
        level: Int,
        experience: Int,
        health: Int,
        maxHealth: Int,
        stamina: Int,
        maxStamina: Int,
        spells: List<String>,
        achievements: List<String>,
        joinDate: Timestamp?,
        lastLogin: Timestamp?,
        lastTaskCompleted: Timestamp?,
        consecutiveTasksCompleted: Int,
        totalTasksCompleted: Int,
        gender: String,
        skinColor: String,
        hairStyle: Int,
        hairColor: String,
        outfit: String,
        createdAt: Timestamp,
        updatedAt: Timestamp
    )


    @Query(
        """
        UPDATE wizards 
        SET 
            gender = :gender,
            skinColor = :skinColor,
            hairStyle = :hairStyle,
            hairColor = :hairColor,
            outfit = :outfit
        WHERE userId = :userId
    """
    )
    suspend fun updateWizardCustomization(
        userId: String,
        gender: String,
        skinColor: String,
        hairStyle: Int,
        hairColor: String,
        outfit: String
    )

    @Query("UPDATE wizards SET lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: Timestamp?)

    @Query("UPDATE wizards SET level = :level, lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLevel(userId: String, level: Int, loginTime: Timestamp?)

    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("UPDATE wizards SET experience = :experience WHERE userId = :userId")
    suspend fun updateExperience(userId: String, experience: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE wizardName = :wizardName)")
    suspend fun isWizardNameExists(wizardName: String): Boolean
}