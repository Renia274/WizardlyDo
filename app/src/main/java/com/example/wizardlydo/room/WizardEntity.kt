package com.example.wizardlydo.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.data.WizardClass
import com.google.firebase.Timestamp
import androidx.room.TypeConverters

@Entity(tableName = "wizards")
@TypeConverters(WizardTypeConverters::class)
data class WizardEntity(
    @PrimaryKey
    val userId: String,
    val wizardClass: WizardClass,
    val wizardName: String,
    val email: String,
    val passwordHash: String,
    val signInProvider: SignInProvider,
    val level: Int = 1,
    val experience: Int = 0,
    // Color and appearance fields
    val gender: String = "Male",
    val skinColor: String = "light",
    val hairColor: String = "brown",
    val hairStyle: Int = 0,
    val outfit: String = "",
    val accessory: String = "",
    // Task-related fields
    val health: Int = 100,
    val maxHealth: Int = 100,
    val lastTaskCompleted: Timestamp? = null,
    val consecutiveTasksCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    // Collection fields
    val spells: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    // Timestamp fields
    val joinDate: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val stamina: Int = 75,

)