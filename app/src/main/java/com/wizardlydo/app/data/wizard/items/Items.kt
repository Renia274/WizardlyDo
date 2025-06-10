package com.wizardlydo.app.data.wizard.items

import com.wizardlydo.app.room.inventory.InventoryItemEntity

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
