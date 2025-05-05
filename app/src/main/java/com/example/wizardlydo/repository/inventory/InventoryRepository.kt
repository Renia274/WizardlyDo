package com.example.wizardlydo.repository.inventory

import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.data.inventory.InventoryItems
import com.example.wizardlydo.room.inventory.InventoryDao
import com.example.wizardlydo.room.inventory.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val inventoryDao: InventoryDao
) {
    fun getInventoryItems(wizardId: String): Flow<List<InventoryItemEntity>> =
        inventoryDao.getInventoryItems(wizardId)

    fun getItemsByType(wizardId: String, itemType: String): Flow<List<InventoryItemEntity>> =
        inventoryDao.getItemsByType(wizardId, itemType)

    suspend fun initializeInventory(wizardProfile: WizardProfile) {
        val items = InventoryItems.getItemsForClass(wizardProfile.wizardClass)
        items.forEach { item ->
            inventoryDao.insertItem(item.copy(wizardId = wizardProfile.userId))
        }
    }

    suspend fun unlockItem(item: InventoryItemEntity) {
        inventoryDao.updateItem(item.copy(isUnlocked = true))
    }

    suspend fun equipItem(wizardId: String, itemId: String) {
        val item = inventoryDao.getItemById(wizardId, itemId)
        item?.let { currentItem ->
            // Unequip all items of the same type
            inventoryDao.unequipAllOfType(wizardId, currentItem.itemType)

            // Equip the selected item
            inventoryDao.updateItem(currentItem.copy(isEquipped = true))
        }
    }

    suspend fun autoUnlockItemsByLevel(wizardId: String, currentLevel: Int) {
        inventoryDao.unlockItemsByLevel(wizardId, currentLevel)
    }
}