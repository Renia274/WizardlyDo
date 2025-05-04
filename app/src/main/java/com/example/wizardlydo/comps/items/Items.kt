package com.example.wizardlydo.comps.items

import com.example.wizardlydo.room.inventory.InventoryItemEntity

object ItemTypes {
    const val OUTFIT = "OUTFIT"
    const val BACKGROUND = "BACKGROUND"

    fun isValid(type: String): Boolean {
        return type in listOf(OUTFIT, BACKGROUND)
    }
}

data class EquippedItems(
    val outfit: InventoryItemEntity? = null,
    val background: InventoryItemEntity? = null
)