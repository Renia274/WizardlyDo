package com.example.wizardlydo.room.wizard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WizardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWizard(wizard: WizardEntity)

    @Query("SELECT * FROM wizards WHERE user_id = :userId")
    suspend fun getWizardById(userId: String): WizardEntity?

    @Query("SELECT * FROM wizards WHERE email = :email")
    suspend fun getWizardByEmail(email: String): WizardEntity?

    @Update
    suspend fun updateWizard(wizard: WizardEntity)


    @Query("UPDATE wizards SET last_task_completed = :lastTaskCompleted, consecutive_tasks_completed = :consecutiveTasksCompleted, total_tasks_completed = :totalTasksCompleted WHERE user_id = :userId")
    suspend fun updateTaskCompletion(userId: String, lastTaskCompleted: Long?, consecutiveTasksCompleted: Int, totalTasksCompleted: Int)

    @Query("UPDATE wizards SET level = :level, experience = :experience WHERE user_id = :userId")
    suspend fun updateLevel(userId: String, level: Int, experience: Int)

    @Query("UPDATE wizards SET skin_color = :skinColor, hair_style = :hairStyle, hair_color = :hairColor, gender = :gender, outfit = :outfit WHERE user_id = :userId")
    suspend fun updateWizardCustomization(userId: String, skinColor: String, hairStyle: Int, hairColor: String,gender: String,outfit: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM wizards WHERE wizard_name = :wizardName)")
    suspend fun isWizardNameExists(wizardName: String): Boolean

    @Query("UPDATE wizards SET health = :health, max_health = :maxHealth WHERE user_id = :userId")
    suspend fun updateHealth(userId: String, health: Int, maxHealth: Int)

    @Query("UPDATE wizards SET stamina = :stamina, max_stamina = :maxStamina WHERE user_id = :userId")
    suspend fun updateStamina(userId: String, stamina: Int, maxStamina: Int)


    @Query("UPDATE wizards SET reminder_enabled = :reminderEnabled, reminder_days = :reminderDays WHERE user_id = :userId")
    suspend fun updateReminderSettings(userId: String, reminderEnabled: Boolean, reminderDays: Int)

    @Query("DELETE FROM wizards WHERE user_id = :userId")
    suspend fun deleteWizardById(userId: String)
}