package com.example.wizardlydo.data.wizard.items

import com.example.wizardlydo.room.inventory.InventoryItemEntity

object ItemTypes {
    const val OUTFIT = "OUTFIT"
    const val BACKGROUND = "BACKGROUND"

}

data class EquippedItems(
    val outfit: InventoryItemEntity? = null,
    val background: InventoryItemEntity? = null,
    val accessory: InventoryItemEntity? = null,
    val weapon: InventoryItemEntity? = null
)