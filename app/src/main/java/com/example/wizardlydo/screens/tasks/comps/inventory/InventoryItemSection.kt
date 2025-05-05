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
import com.example.wizardlydo.data.inventory.ItemType
import com.example.wizardlydo.data.wizard.items.ItemTypes
import com.example.wizardlydo.room.inventory.InventoryItemEntity

@Composable
fun InventoryItemsSection(
    items: List<InventoryItemEntity>,
    wizardLevel: Int,
    onEquipItem: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Text(
            text = "Outfits",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = items.filter { it.itemType == ItemType.OUTFIT.toString() },
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )

        Spacer(modifier = Modifier.height(24.dp))


        Text(
            text = "Backgrounds",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemGrid(
            items = items.filter { it.itemType == ItemType.BACKGROUND.toString() },
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )


        val accessoryItems = items.filter { it.itemType == ItemType.ACCESSORY.toString() }
        if (accessoryItems.isNotEmpty() && wizardLevel >= 15) {
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
        }


        val weaponItems = items.filter { it.itemType == ItemType.WEAPON.toString() }
        if (weaponItems.isNotEmpty() && wizardLevel == 30) {
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
        }
    }
}