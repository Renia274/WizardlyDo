package com.example.wizardlydo.screens.tasks.comps.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.room.inventory.InventoryItemEntity

@Composable
fun ItemGrid(
    items: List<InventoryItemEntity>,
    wizardLevel: Int,
    onEquipItem: (String) -> Unit
) {
    var selectedItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(items) { item ->
            InventoryItemCard(
                item = item,
                isLocked = item.unlockLevel > wizardLevel,
                onEquip = { selectedItem = item }
            )
        }
    }

    selectedItem?.let { item ->
        ItemDetailsDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onEquip = { onEquipItem(item.itemId) },
            isEquipped = item.isEquipped
        )
    }
}