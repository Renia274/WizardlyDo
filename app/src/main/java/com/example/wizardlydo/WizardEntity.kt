package com.example.wizardlydo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "wizards")
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
    val spells: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val joinDate: Date? = null,
    val lastLogin: Date? = null)