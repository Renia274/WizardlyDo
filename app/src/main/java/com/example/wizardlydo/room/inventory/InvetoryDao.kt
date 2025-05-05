package com.example.wizardlydo.room.inventory

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE wizard_id = :wizardId")
    fun getInventoryItems(wizardId: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("SELECT * FROM inventory_items WHERE wizard_id = :wizardId AND item_id = :itemId")
    suspend fun getItemById(wizardId: String, itemId: String): InventoryItemEntity?

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("SELECT * FROM inventory_items WHERE wizard_id = :wizardId AND item_type = :itemType")
    fun getItemsByType(wizardId: String, itemType: String): Flow<List<InventoryItemEntity>>

    @Query("UPDATE inventory_items SET is_equipped = 0 WHERE wizard_id = :wizardId AND item_type = :itemType")
    suspend fun unequipAllOfType(wizardId: String, itemType: String)

    @Query("UPDATE inventory_items SET is_unlocked = 1 WHERE wizard_id = :wizardId AND unlock_level <= :currentLevel")
    suspend fun unlockItemsByLevel(wizardId: String, currentLevel: Int)
}
