package com.example.wizardlydo.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_pins")
data class PinEntity(
    @PrimaryKey val id: Int = 1,
    val encryptedPin: String,
    val biometricsEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)