package com.wizardlydo.app.room.wizard

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wizardlydo.app.providers.SignInProvider
import com.wizardlydo.app.data.wizard.WizardClass
import com.google.firebase.Timestamp
import androidx.room.TypeConverters
import com.wizardlydo.app.room.WizardTypeConverters

@Entity(tableName = "wizards")
@TypeConverters(WizardTypeConverters::class)
data class WizardEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "wizard_class")
    val wizardClass: WizardClass,

    @ColumnInfo(name = "wizard_name")
    val wizardName: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "sign_in_provider")
    val signInProvider: SignInProvider,

    @ColumnInfo(name = "level", defaultValue = "1")
    val level: Int = 1,

    @ColumnInfo(name = "experience", defaultValue = "0")
    val experience: Int = 0,

    @ColumnInfo(name = "gender", defaultValue = "Male")
    val gender: String = "Male",

    @ColumnInfo(name = "skin_color", defaultValue = "light")
    val skinColor: String = "light",

    @ColumnInfo(name = "hair_color", defaultValue = "brown")
    val hairColor: String = "brown",

    @ColumnInfo(name = "hair_style", defaultValue = "0")
    val hairStyle: Int = 0,

    @ColumnInfo(name = "outfit", defaultValue = "")
    val outfit: String = "",

    // Task-related fields
    @ColumnInfo(name = "health", defaultValue = "100")
    val health: Int = 100,

    @ColumnInfo(name = "max_health", defaultValue = "100")
    val maxHealth: Int = 100,

    @ColumnInfo(name = "stamina", defaultValue = "50")
    val stamina: Int = 50,

    @ColumnInfo(name = "max_stamina", defaultValue = "100")
    val maxStamina: Int = 100,

    @ColumnInfo(name = "last_task_completed")
    val lastTaskCompleted: Timestamp? = null,

    @ColumnInfo(name = "consecutive_tasks_completed", defaultValue = "0")
    val consecutiveTasksCompleted: Int = 0,

    @ColumnInfo(name = "total_tasks_completed", defaultValue = "0")
    val totalTasksCompleted: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long? = null,

    // Notification settings
    @ColumnInfo(name = "reminder_enabled", defaultValue = "1")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "reminder_days", defaultValue = "1")
    val reminderDays: Int = 1,

    @ColumnInfo(name = "in_app_notifications_enabled", defaultValue = "1")
    val inAppNotificationsEnabled: Boolean = true,

    @ColumnInfo(name = "damage_notifications_enabled", defaultValue = "1")
    val damageNotificationsEnabled: Boolean = true
)
