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
import com.example.wizardlydo.comps.items.ItemTypes
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
            items = items.filter { it.itemType == ItemTypes.OUTFIT },
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
            items = items.filter { it.itemType == ItemTypes.BACKGROUND },
            wizardLevel = wizardLevel,
            onEquipItem = onEquipItem
        )
    }
}