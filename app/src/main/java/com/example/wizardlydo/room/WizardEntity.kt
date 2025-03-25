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
    val spells: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val joinDate: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val gender: String = "Male",
    val bodyColor: String = "#FFD700",
    val clothingColor: String = "#2E0854",
    val accessoryColor: String = "#000000"
)