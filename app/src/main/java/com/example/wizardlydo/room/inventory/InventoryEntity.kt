package com.example.wizardlydo.room.inventory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    indices = [Index(value = ["wizard_id", "item_id"], unique = true)]
)
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "wizard_id") val wizardId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "item_type") val itemType: String,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean,
    @ColumnInfo(name = "is_equipped") val isEquipped: Boolean,
    @ColumnInfo(name = "unlock_level") val unlockLevel: Int,
    @ColumnInfo(name = "resource_id") val resourceId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String
)
