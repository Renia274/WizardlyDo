package com.example.wizardlydo.repository.pin

import com.example.wizardlydo.room.PinDao
import com.example.wizardlydo.room.PinEntity
import com.example.wizardlydo.utilities.SecurityProvider

class PinRepository(
    private val pinDao: PinDao,
    private val securityProvider: SecurityProvider
) {
    suspend fun savePin(pin: String, enableBiometrics: Boolean = false): Result<Unit> {
        return try {
            // Encrypt the PIN
            val encryptedPin = securityProvider.encrypt(pin)

            // Create or update PIN entity
            val pinEntity = PinEntity(
                encryptedPin = encryptedPin,
                biometricsEnabled = enableBiometrics
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

    suspend fun updateBiometricPreference(enabled: Boolean): Result<Unit> {
        return try {
            val currentPin = pinDao.getPin()
                ?: throw Exception("No PIN set")

            val updatedPin = currentPin.copy(biometricsEnabled = enabled)
            pinDao.insertPin(updatedPin)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasPinSet(): Boolean {
        return pinDao.getPinCount() > 0
    }

    suspend fun isBiometricsEnabled(): Boolean {
        return pinDao.getPin()?.biometricsEnabled ?: false
    }
}