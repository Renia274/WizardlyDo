package com.example.wizardlydo.repository.pin

import com.example.wizardlydo.room.pin.PinDao
import com.example.wizardlydo.room.pin.PinEntity
import com.example.wizardlydo.utilities.SecurityProvider

class PinRepository(
    private val pinDao: PinDao,
    private val securityProvider: SecurityProvider
) {
    suspend fun savePin(pin: String): Result<Unit> {
        return try {
            // Encrypt the PIN
            val encryptedPin = securityProvider.encrypt(pin)

            // Create or update PIN entity
            val pinEntity = PinEntity(
                encryptedPin = encryptedPin
            )

            // Insert or replace
            pinDao.insertPin(pinEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validatePin(inputPin: String): Result<Boolean> {
        return try {
            // Retrieve the stored encrypted PIN
            val storedPin = pinDao.getPin()
                ?: return Result.success(false)

            // Decrypt and compare
            val decryptedStoredPin = securityProvider.decrypt(storedPin.encryptedPin)

            Result.success(inputPin == decryptedStoredPin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun hasPinSet(): Boolean {
        return pinDao.getPinCount() > 0
    }

}