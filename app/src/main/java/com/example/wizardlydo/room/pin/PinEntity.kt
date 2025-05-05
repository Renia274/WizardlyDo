package com.example.wizardlydo.room.pin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_pins")
data class PinEntity(
    @PrimaryKey val id: Int = 1,
    val encryptedPin: String,
    val createdAt: Long = System.currentTimeMillis()
)