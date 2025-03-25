package com.example.wizardlydo.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wizardlydo.WizardClass
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
            spells = :spells,
            achievements = :achievements,
            joinDate = :joinDate,
            lastLogin = :lastLogin,
            gender = :gender,
            bodyColor = :bodyColor,
            clothingColor = :clothingColor,
            accessoryColor = :accessoryColor
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
        spells: List<String>,
        achievements: List<String>,
        joinDate: Timestamp?,
        lastLogin: Timestamp?,
        gender: String,
        bodyColor: String,
        clothingColor: String,
        accessoryColor: String
    )

    // Change parameters to Long (timestamp milliseconds)
    @Query("UPDATE wizards SET lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: Long?)

    @Query("UPDATE wizards SET level = :level, lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLevel(userId: String, level: Int, loginTime: Long?)



    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("UPDATE wizards SET experience = :experience WHERE userId = :userId")
    suspend fun updateExperience(userId: String, experience: Int)

    @Query("UPDATE wizards SET level = :level, lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLevel(userId: String, level: Int, loginTime: Timestamp)


}