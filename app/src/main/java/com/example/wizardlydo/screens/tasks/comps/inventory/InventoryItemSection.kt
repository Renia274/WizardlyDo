package com.example.wizardlydo.screens.tasks.comps.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.inventory.InventoryItems
import com.example.wizardlydo.data.inventory.ItemType
import com.example.wizardlydo.room.inventory.InventoryItemEntity

@Composable
fun InventoryItemsSection(
    items: List<InventoryItemEntity>,
    wizardLevel: Int,
    onEquipItem: (String) -> Unit
) {
    // Get all  items by type
    val outfitItems = items.filter { it.itemType == ItemType.OUTFIT.toString() }
    val backgroundItems = items.filter { it.itemType == ItemType.BACKGROUND.toString() }
    val accessoryItems = if (items.any { it.itemType == ItemType.ACCESSORY.toString() }) {
        items.filter { it.itemType == ItemType.ACCESSORY.toString() }
    } else {
        InventoryItems.commonItems.filter { it.itemType == ItemType.ACCESSORY.toString() }
    }
    val weaponItems = if (items.any { it.itemType == ItemType.WEAPON.toString() }) {
        items.filter { it.itemType == ItemType.WEAPON.toString() }
    } else {
        InventoryItems.commonItems.filter { it.itemType == ItemType.WEAPON.toString() }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Outfits Section
        Text(
            text = "Outfits",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = outfitItems,
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Backgrounds Section
        Text(
            text = "Backgrounds",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = backgroundItems,
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )

        // Accessories Section
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Accessories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = accessoryItems,
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )

        // Weapons Section
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Weapons",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = weaponItems,
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}