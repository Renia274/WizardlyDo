package com.example.wizardlydo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WizardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWizard(wizard: WizardEntity)

    @Query("SELECT * FROM wizards WHERE userId = :userId")
    suspend fun getWizardById(userId: String): WizardEntity?

    @Query("SELECT * FROM wizards WHERE email = :email")
    suspend fun getWizardByEmail(email: String): WizardEntity?

    @Update
    suspend fun updateWizard(wizard: WizardEntity)

    @Query("UPDATE wizards SET lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("UPDATE wizards SET experience = :experience WHERE userId = :userId")
    suspend fun updateExperience(userId: String, experience: Int)

    @Query("UPDATE wizards SET level = :level, lastLogin = :loginTime WHERE userId = :userId")
    suspend fun updateLevel(userId: String, level: Int, loginTime: Long)
}