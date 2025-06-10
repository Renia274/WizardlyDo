package com.wizardlydo.app.room.pin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: PinEntity)

    @Query("SELECT * FROM security_pins LIMIT 1")
    suspend fun getPin(): PinEntity?

    @Query("SELECT COUNT(*) FROM security_pins")
    suspend fun getPinCount(): Int

    @Query("DELETE FROM security_pins")
    suspend fun clearPin()

    @Query("UPDATE security_pins SET encrypted_pin = :newEncryptedPin WHERE id = 1")
    suspend fun updatePin(newEncryptedPin: String)
}
